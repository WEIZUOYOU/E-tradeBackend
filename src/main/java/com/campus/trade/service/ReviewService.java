package com.campus.trade.service;

import com.campus.trade.dto.request.SendMessageRequest;
import com.campus.trade.entity.Review;
import com.campus.trade.entity.Trade;
import com.campus.trade.entity.User;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.ReviewRepository;
import com.campus.trade.repository.TradeRepository;
import com.campus.trade.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价服务
 */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditService creditService;

    @Autowired
    private MessageService messageService;

    // 交易状态常量
    private static final int STATUS_COMPLETED = 4;      // 已完成，等待评价
    private static final int STATUS_BUYER_REVIEWED = 6;  // 买家已评价
    private static final int STATUS_SELLER_REVIEWED = 7;  // 卖家已评价
    private static final int STATUS_BOTH_REVIEWED = 8;    // 双方已评价

    /**
     * 提交评价
     * 
     * @param reviewerId 评价者ID
     * @param tradeId 交易ID
     * @param rating 评分（1-5）
     * @param content 评价内容
     * @param tags 评价标签
     */
    @Transactional
    public void createReview(Long reviewerId, Long tradeId, Integer rating, String content, String tags) {
        // 1. 检查交易是否存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(4001, "交易不存在");
        }

        // 2. 检查评价者身份（必须是买家或卖家）
        boolean isBuyer = trade.getBuyerId().equals(reviewerId);
        boolean isSeller = trade.getSellerId().equals(reviewerId);
        
        if (!isBuyer && !isSeller) {
            throw new BusinessException(4002, "无权评价该交易");
        }

        // 3. 检查交易状态是否允许评价
        // 允许评价的状态：4(已完成)、6(买家已评价)、7(卖家已评价)
        // - 状态4：双方都可以评价
        // - 状态6：只有卖家可以评价（买家已评价，等待卖家评价）
        // - 状态7：只有买家可以评价（卖家已评价，等待买家评价）
        int currentStatus = trade.getStatus();
        boolean canReview = false;
        
        if (currentStatus == STATUS_COMPLETED) {
            // 状态4：双方都可以评价
            canReview = true;
        } else if (currentStatus == STATUS_BUYER_REVIEWED && isSeller) {
            // 状态6：只有卖家可以评价
            canReview = true;
        } else if (currentStatus == STATUS_SELLER_REVIEWED && isBuyer) {
            // 状态7：只有买家可以评价
            canReview = true;
        }
        
        if (!canReview) {
            throw new BusinessException(4004, "交易状态不允许评价");
        }

        // 4. 检查是否评价自己
        if (isBuyer && reviewerId.equals(trade.getSellerId())) {
            throw new BusinessException(4005, "不能评价自己");
        }
        if (isSeller && reviewerId.equals(trade.getBuyerId())) {
            throw new BusinessException(4005, "不能评价自己");
        }

        // 5. 检查是否已评价过
        if (reviewRepository.hasReviewed(tradeId, reviewerId)) {
            throw new BusinessException(4003, "该交易已完成评价");
        }

        // 6. 确定被评价者
        Long revieweeId = isBuyer ? trade.getSellerId() : trade.getBuyerId();

        // 7. 获取评价者和被评价者信息
        User reviewer = userRepository.findById(reviewerId);
        User reviewee = userRepository.findById(revieweeId);
        
        String reviewerName = reviewer != null ? reviewer.getUsername() : "";
        String revieweeName = reviewee != null ? reviewee.getUsername() : "";

        // 8. 保存评价
        Review review = new Review();
        review.setTradeId(tradeId);
        review.setTradeNo(trade.getTradeNo());
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setRevieweeId(revieweeId);
        review.setRevieweeName(revieweeName);
        review.setReviewerType(isBuyer ? 0 : 1); // 0-买家，1-卖家
        review.setRating(rating);
        review.setContent(content);
        review.setTags(tags);
        review.setProductName(trade.getProductName());
        review.setProductImage(trade.getProductImage());
        reviewRepository.insert(review);

        // 9. 更新交易状态
        int newStatus = updateTradeStatus(tradeId, isBuyer);

        // 10. 更新信用分
        updateCreditScore(revieweeId, rating);

        // 11. 发送评价通知消息
        sendReviewNotification(trade, reviewerId, revieweeId, newStatus);
    }

    /**
     * 更新交易状态
     * - 买家评价后：4 → 6
     * - 卖家评价后（买家已评价）：6 → 8
     * - 卖家评价后（卖家先评价）：4 → 7
     * - 买家评价后（卖家已评价）：7 → 8
     * @return 更新后的状态
     */
    private int updateTradeStatus(Long tradeId, boolean isBuyer) {
        Trade trade = tradeRepository.findById(tradeId);
        int currentStatus = trade.getStatus();
        int newStatus;

        if (isBuyer) {
            // 买家评价
            if (currentStatus == STATUS_COMPLETED) {
                newStatus = STATUS_BUYER_REVIEWED; // 4 → 6
            } else if (currentStatus == STATUS_SELLER_REVIEWED) {
                newStatus = STATUS_BOTH_REVIEWED;   // 7 → 8
            } else {
                newStatus = STATUS_BUYER_REVIEWED;
            }
        } else {
            // 卖家评价
            if (currentStatus == STATUS_COMPLETED) {
                newStatus = STATUS_SELLER_REVIEWED; // 4 → 7
            } else if (currentStatus == STATUS_BUYER_REVIEWED) {
                newStatus = STATUS_BOTH_REVIEWED;   // 6 → 8
            } else {
                newStatus = STATUS_SELLER_REVIEWED;
            }
        }

        tradeRepository.updateStatus(tradeId, newStatus);
        return newStatus;
    }

    /**
     * 发送评价通知消息
     * @param trade 交易信息
     * @param reviewerId 评价者ID
     * @param revieweeId 被评价者ID
     * @param newStatus 新的交易状态
     */
    private void sendReviewNotification(Trade trade, Long reviewerId, Long revieweeId, int newStatus) {
        try {
            String messageContent;
            switch (newStatus) {
                case STATUS_BUYER_REVIEWED:
                    messageContent = "买家已评价";
                    break;
                case STATUS_SELLER_REVIEWED:
                    messageContent = "卖家已评价";
                    break;
                case STATUS_BOTH_REVIEWED:
                    messageContent = "双方已评价";
                    break;
                default:
                    messageContent = "评价已完成";
            }

            SendMessageRequest request = new SendMessageRequest();
            request.setReceiverId(revieweeId);
            request.setProductId(trade.getProductId());
            request.setContent(messageContent);
            request.setType(1); // 交易卡片消息
            request.setTradeId(trade.getId());
            request.setTradeStatus(newStatus);

            // 发送消息（使用消息服务）
            messageService.sendMessage(reviewerId, request);
            
            log.info("评价通知消息已发送: reviewerId={}, revieweeId={}, tradeId={}, status={}", 
                    reviewerId, revieweeId, trade.getId(), newStatus);
        } catch (Exception e) {
            // 消息发送失败不影响评价流程
            log.error("发送评价通知消息失败: tradeId={}, error={}", trade.getId(), e.getMessage());
        }
    }

    /**
     * 更新信用分
     * 好评（4-5星）+5分，中评（3星）+0分，差评（1-2星）-10分
     */
    private void updateCreditScore(Long userId, Integer rating) {
        int scoreChange;
        if (rating >= 4) {
            scoreChange = 5;
        } else if (rating == 3) {
            scoreChange = 0;
        } else {
            scoreChange = -10;
        }
        userRepository.updateCreditScore(userId, scoreChange);
        creditService.updateReviewRate(userId);
    }

    /**
     * 获取交易的所有评价
     */
    public List<Review> getTradeReviews(Long tradeId) {
        return reviewRepository.findByTradeId(tradeId);
    }

    /**
     * 根据ID获取评价详情
     */
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    /**
     * 删除评价（需权限校验）
     */
    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id);
        if (review == null) {
            throw new BusinessException(4006, "评价不存在");
        }
        
        // 权限校验：只有评价者本人可以删除评价
        if (!review.getReviewerId().equals(userId)) {
            throw new BusinessException(4007, "无权删除该评价");
        }
        
        // 获取相关数据用于后续处理
        Long tradeId = review.getTradeId();
        Long revieweeId = review.getRevieweeId();
        Integer rating = review.getRating();
        Integer reviewerType = review.getReviewerType(); // 0-买家，1-卖家
        
        // 删除评价记录
        reviewRepository.deleteById(id);
        
        // 恢复信用分（反向计算）
        restoreCreditScore(revieweeId, rating);
        
        // 更新交易状态
        restoreTradeStatus(tradeId, reviewerType);
    }

    /**
     * 恢复信用分（删除评价时）
     * 好评（4-5星）-5分，中评（3星）-0分，差评（1-2星）+10分
     */
    private void restoreCreditScore(Long userId, Integer rating) {
        int scoreChange;
        if (rating >= 4) {
            scoreChange = -5;  // 撤销好评，扣回5分
        } else if (rating == 3) {
            scoreChange = 0;   // 中评无变化
        } else {
            scoreChange = 10;  // 撤销差评，恢复10分
        }
        userRepository.updateCreditScore(userId, scoreChange);
        creditService.updateReviewRate(userId);
    }

    /**
     * 恢复交易状态（删除评价时）
     * - 删除买家评价：状态6→4，状态8→7
     * - 删除卖家评价：状态7→4，状态8→6
     */
    private void restoreTradeStatus(Long tradeId, Integer reviewerType) {
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            return;
        }
        
        int currentStatus = trade.getStatus();
        int newStatus = currentStatus;
        
        if (reviewerType == 0) {
            // 删除买家评价
            if (currentStatus == STATUS_BUYER_REVIEWED) {
                newStatus = STATUS_COMPLETED; // 6 → 4
            } else if (currentStatus == STATUS_BOTH_REVIEWED) {
                newStatus = STATUS_SELLER_REVIEWED; // 8 → 7
            }
        } else {
            // 删除卖家评价
            if (currentStatus == STATUS_SELLER_REVIEWED) {
                newStatus = STATUS_COMPLETED; // 7 → 4
            } else if (currentStatus == STATUS_BOTH_REVIEWED) {
                newStatus = STATUS_BUYER_REVIEWED; // 8 → 6
            }
        }
        
        if (newStatus != currentStatus) {
            tradeRepository.updateStatus(tradeId, newStatus);
        }
    }

    /**
     * 获取用户收到的所有评价
     */
    public List<Review> getReceivedReviews(Long userId) {
        return reviewRepository.findReceivedReviews(userId);
    }

    /**
     * 获取用户给出的所有评价
     */
    public List<Review> getGivenReviews(Long userId) {
        return reviewRepository.findGivenReviews(userId);
    }

    /**
     * 获取用户收到的评价数量
     */
    public int getReceivedReviewCount(Long userId) {
        return reviewRepository.countReceived(userId);
    }

    /**
     * 获取用户给出的评价数量
     */
    public int getGivenReviewCount(Long userId) {
        return reviewRepository.countGiven(userId);
    }

    /**
     * 获取用户给出的评价的平均评分
     */
    public double getGivenAverageRating(Long userId) {
        return reviewRepository.getGivenAverageRating(userId);
    }

    /**
     * 获取用户收到的平均评分
     */
    public double getAverageRating(Long userId) {
        return reviewRepository.getAverageRating(userId);
    }

    /**
     * 获取用户评价统计
     */
    public Map<String, Object> getReviewStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 收到的评价数量
        int receivedCount = reviewRepository.countReceived(userId);
        stats.put("receivedCount", receivedCount);
        
        // 给出的评价数量
        int givenCount = reviewRepository.countGiven(userId);
        stats.put("givenCount", givenCount);
        
        // 平均评分
        double avgRating = reviewRepository.getAverageRating(userId);
        stats.put("averageRating", Math.round(avgRating * 10) / 10.0);
        
        // 好评率
        double goodReviewRate = reviewRepository.getGoodReviewRate(userId);
        stats.put("goodReviewRate", Math.round(goodReviewRate * 100));
        
        return stats;
    }
}
