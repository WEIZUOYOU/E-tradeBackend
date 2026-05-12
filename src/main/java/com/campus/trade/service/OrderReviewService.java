package com.campus.trade.service;

import com.campus.trade.dto.request.CreateReviewRequest;
import com.campus.trade.dto.response.ReviewResponse;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.OrderRepository;
import com.campus.trade.repository.OrderReviewRepository;
import com.campus.trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderReviewService {

    @Autowired
    private OrderReviewRepository orderReviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditService creditService;

    @Transactional
    public void createReview(Long reviewerId, CreateReviewRequest req) {
        // 校验订单存在且已完成
        Order order = orderRepository.findByOrderNo(req.getOrderNo());
        if (order == null || order.getStatus() != 3) {
            throw new BusinessException("订单不存在或未完成，无法评价");
        }

        // 校验评价者身份
        boolean isBuyer = order.getBuyerId().equals(reviewerId);
        boolean isSeller = order.getSellerId().equals(reviewerId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException("无权评价该订单");
        }

        // 确定评价类型和被评价者
        Integer reviewType;
        Long revieweeId;
        if (isBuyer) {
            reviewType = 0; // 买家评卖家
            revieweeId = order.getSellerId();
        } else {
            reviewType = 1; // 卖家评买家
            revieweeId = order.getBuyerId();
        }

        // 检查是否已评价
        if (orderReviewRepository.hasReviewed(order.getId(), reviewerId, reviewType)) {
            throw new BusinessException("您已评价过该订单");
        }

        // 保存评价
        OrderReview review = new OrderReview();
        review.setOrderId(order.getId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setReviewType(reviewType);
        review.setRating(req.getRating());
        review.setContent(req.getContent());
        orderReviewRepository.insert(review);

        // 更新信用分
        updateCreditScore(revieweeId, req.getRating());
        // 更新好评率统计
        creditService.updateReviewRate(revieweeId);
    }

    private void updateCreditScore(Long userId, Integer rating) {
        // 好评（4-5星）+5分，中评（3星）+0，差评（1-2星）-10分
        int scoreChange;
        if (rating >= 4) {
            scoreChange = 5;
        } else if (rating == 3) {
            scoreChange = 0;
        } else {
            scoreChange = -10;
        }
        userRepository.updateCreditScore(userId, scoreChange);
    }

    public List<ReviewResponse> getOrderReviews(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        List<OrderReview> reviews = orderReviewRepository.findByOrderId(order.getId());
        return reviews.stream().map(review -> {
            ReviewResponse resp = new ReviewResponse();
            resp.setId(review.getId());
            resp.setOrderId(review.getOrderId());
            resp.setReviewerId(review.getReviewerId());
            resp.setRevieweeId(review.getRevieweeId());
            resp.setReviewType(review.getReviewType());
            resp.setRating(review.getRating());
            resp.setContent(review.getContent());
            resp.setCreateTime(review.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
    }
}
