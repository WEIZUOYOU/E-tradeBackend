package com.campus.trade.repository;

import com.campus.trade.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价仓库
 */
@Repository
public class ReviewRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 创建评价
    public Long insert(Review review) {
        String sql = "INSERT INTO review (trade_id, reviewer_id, reviewee_id, reviewer_type, rating, content, tags, create_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, 
            review.getTradeId(),
            review.getReviewerId(),
            review.getRevieweeId(),
            review.getReviewerType(),
            review.getRating(),
            review.getContent(),
            review.getTags()
        );
        
        // 获取自增ID
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id;
    }

    // 检查用户是否已评价该交易
    public boolean hasReviewed(Long tradeId, Long reviewerId) {
        String sql = "SELECT COUNT(*) FROM review WHERE trade_id = ? AND reviewer_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tradeId, reviewerId);
        return count != null && count > 0;
    }

    // 获取交易的所有评价
    public List<Review> findByTradeId(Long tradeId) {
        String sql = "SELECT * FROM review WHERE trade_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new ReviewWithProductImageRowMapper(), tradeId);
    }

    // 获取用户收到的所有评价（包含商品图片）
    public List<Review> findReceivedReviews(Long userId) {
        String sql = "SELECT r.*, t.product_image FROM review r " +
                     "JOIN trade t ON r.trade_id = t.id " +
                     "WHERE r.reviewee_id = ? ORDER BY r.create_time DESC";
        return jdbcTemplate.query(sql, new ReviewWithProductImageRowMapper(), userId);
    }

    // 获取用户给出的所有评价（包含商品图片）
    public List<Review> findGivenReviews(Long userId) {
        String sql = "SELECT r.*, t.product_image FROM review r " +
                     "JOIN trade t ON r.trade_id = t.id " +
                     "WHERE r.reviewer_id = ? ORDER BY r.create_time DESC";
        return jdbcTemplate.query(sql, new ReviewWithProductImageRowMapper(), userId);
    }

    // 统计用户收到的评价数量
    public int countReceived(Long userId) {
        String sql = "SELECT COUNT(*) FROM review WHERE reviewee_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // 统计用户给出的评价数量
    public int countGiven(Long userId) {
        String sql = "SELECT COUNT(*) FROM review WHERE reviewer_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // 计算用户收到的平均评分
    public double getAverageRating(Long userId) {
        String sql = "SELECT AVG(rating) FROM review WHERE reviewee_id = ?";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, userId);
        return avg != null ? avg : 0.0;
    }

    // 计算好评率（评分>=4的比例）
    public double getGoodReviewRate(Long userId) {
        String totalSql = "SELECT COUNT(*) FROM review WHERE reviewee_id = ?";
        Integer total = jdbcTemplate.queryForObject(totalSql, Integer.class, userId);
        if (total == null || total == 0) {
            return 0.0;
        }
        
        String goodSql = "SELECT COUNT(*) FROM review WHERE reviewee_id = ? AND rating >= 4";
        Integer good = jdbcTemplate.queryForObject(goodSql, Integer.class, userId);
        
        return (double) good / total;
    }

    // 行映射器（包含商品图片）
    private static class ReviewWithProductImageRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review();
            review.setId(rs.getLong("id"));
            review.setTradeId(rs.getLong("trade_id"));
            review.setReviewerId(rs.getLong("reviewer_id"));
            review.setRevieweeId(rs.getLong("reviewee_id"));
            review.setReviewerType(rs.getInt("reviewer_type"));
            review.setRating(rs.getInt("rating"));
            review.setContent(rs.getString("content"));
            review.setTags(rs.getString("tags"));
            review.setProductImage(rs.getString("product_image"));
            review.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            review.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
            return review;
        }
    }
}
