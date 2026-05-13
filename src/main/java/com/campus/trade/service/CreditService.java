package com.campus.trade.service;

import com.campus.trade.dto.response.CreditDetailResponse;
import com.campus.trade.dto.response.ReviewHistoryResponse;
import com.campus.trade.entity.OrderReview;
import com.campus.trade.entity.User;
import com.campus.trade.entity.UserCredit;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.CreditRepository;
import com.campus.trade.repository.OrderReviewRepository;
import com.campus.trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditService {

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderReviewRepository orderReviewRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 获取用户信用档案
    public CreditDetailResponse getCreditDetail(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 计算交易次数
        Integer buyerCount = creditRepository.getBuyerTradeCount(userId);
        Integer sellerCount = creditRepository.getSellerTradeCount(userId);
        Integer totalTradeCount = buyerCount + sellerCount;

        // 计算好评率
        Integer totalReviews = creditRepository.getReviewCount(userId);
        Integer goodReviews = creditRepository.getGoodReviewCount(userId);
        BigDecimal goodReviewRate = BigDecimal.ZERO;
        if (totalReviews > 0) {
            goodReviewRate = BigDecimal.valueOf(goodReviews)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP);
        }

        // 构建响应
        CreditDetailResponse response = new CreditDetailResponse();
        response.setUserId(userId);
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setCreditScore(user.getCreditScore() != null ? user.getCreditScore() : 100);
        response.setTradeCount(totalTradeCount);
        response.setGoodReviewRate(goodReviewRate.doubleValue());
        response.setTotalReviews(totalReviews);
        response.setGoodReviews(goodReviews);

        return response;
    }

    // 获取用户收到的评价历史（分页）
    public List<ReviewHistoryResponse> getReviewHistory(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT orev.*, u.username as reviewer_name, u.avatar as reviewer_avatar " +
                "FROM order_review orev " +
                "JOIN user u ON orev.reviewer_id = u.id " +
                "WHERE orev.reviewee_id = ? " +
                "ORDER BY orev.create_time DESC " +
                "LIMIT ? OFFSET ?";

        List<ReviewHistoryResponse> reviews = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReviewHistoryResponse resp = new ReviewHistoryResponse();
            resp.setId(rs.getLong("id"));
            resp.setOrderId(rs.getLong("order_id"));
            resp.setReviewerId(rs.getLong("reviewer_id"));
            resp.setReviewerName(rs.getString("reviewer_name"));
            resp.setReviewerAvatar(rs.getString("reviewer_avatar"));
            resp.setReviewType(rs.getInt("review_type"));
            resp.setRating(rs.getInt("rating"));
            resp.setContent(rs.getString("content"));
            resp.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            return resp;
        }, userId, size, offset);

        return reviews;
    }

    // 初始化用户信用档案（注册时调用）
    @Transactional
    public void initializeCredit(Long userId) {
        try {
            creditRepository.insert(userId);
        } catch (Exception e) {
            // 如果已存在则忽略
        }
    }

    // 更新好评率（在订单评价后调用）
    @Transactional
    public void updateReviewRate(Long userId) {
        Integer totalReviews = creditRepository.getReviewCount(userId);
        Integer goodReviews = creditRepository.getGoodReviewCount(userId);
        BigDecimal rate = BigDecimal.ZERO;
        if (totalReviews > 0) {
            rate = BigDecimal.valueOf(goodReviews)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP);
        }
        creditRepository.updateGoodReviewRate(userId, rate);
    }

    // 获取用户好评率（用于排序/展示）
    public double getGoodReviewRatePercentage(Long userId) {
        Integer totalReviews = creditRepository.getReviewCount(userId);
        if (totalReviews == null || totalReviews == 0) {
            return 0.0;
        }
        Integer goodReviews = creditRepository.getGoodReviewCount(userId);
        return (double) goodReviews / totalReviews * 100;
    }

}
