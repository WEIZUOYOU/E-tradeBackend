package com.campus.trade.repository;

import com.campus.trade.entity.OrderReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class OrderReviewRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 插入评价
    public Long insert(OrderReview review) {
        String sql = "INSERT INTO order_review(order_id, reviewer_id, reviewee_id, review_type, rating, content) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getOrderId());
            ps.setLong(2, review.getReviewerId());
            ps.setLong(3, review.getRevieweeId());
            ps.setInt(4, review.getReviewType());
            ps.setInt(5, review.getRating());
            ps.setString(6, review.getContent());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    // 查询订单的评价（支持互评）
    public List<OrderReview> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_review WHERE order_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderReview.class), orderId);
    }

    // 检查用户是否已评价该订单的对方
    public boolean hasReviewed(Long orderId, Long reviewerId, Integer reviewType) {
        String sql = "SELECT COUNT(*) FROM order_review WHERE order_id = ? AND reviewer_id = ? AND review_type = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, orderId, reviewerId, reviewType);
        return count != null && count > 0;
    }

    // 查询用户收到的所有评价
    public List<OrderReview> findByRevieweeId(Long revieweeId) {
        String sql = "SELECT * FROM order_review WHERE reviewee_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderReview.class), revieweeId);
    }
}
