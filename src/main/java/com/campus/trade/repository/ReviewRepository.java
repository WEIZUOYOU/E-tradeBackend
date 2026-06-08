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
        String sql = "INSERT INTO review (trade_id, trade_no, reviewer_id, reviewer_name, reviewee_id, reviewee_name, " +
                     "reviewer_type, rating, content, tags, product_name, product_image, create_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, 
            review.getTradeId(),
            review.getTradeNo(),
            review.getReviewerId(),
            review.getReviewerName(),
            review.getRevieweeId(),
            review.getRevieweeName(),
            review.getReviewerType(),
            review.getRating(),
            review.getContent(),
            review.getTags(),
            review.getProductName(),
            review.getProductImage()
        );
        
        // 获取自增ID
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id;
    }

    // 根据ID获取评价
    public Review findById(Long id) {
        String sql = "SELECT * FROM review WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, new FullReviewRowMapper(), id);
        return reviews.isEmpty() ? null : reviews.get(0);
    }

    // 删除评价
    public int deleteById(Long id) {
        String sql = "DELETE FROM review WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 检查用户是否已评价该交易
    public boolean hasReviewed(Long tradeId, Long reviewerId) {
        String sql = "SELECT COUNT(*) FROM review WHERE trade_id = ? AND reviewer_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tradeId, reviewerId);
        return count != null && count > 0;
    }

    // 获取交易的所有评价
    public List<Review> findByTradeId(Long tradeId) {
        String sql = "SELECT r.* FROM review r WHERE r.trade_id = ? ORDER BY r.create_time DESC";
        return jdbcTemplate.query(sql, new FullReviewRowMapper(), tradeId);
    }

    // 获取用户收到的所有评价（完整信息）
    public List<Review> findReceivedReviews(Long userId) {
        String sql = "SELECT r.id, r.trade_id, r.trade_no, r.reviewer_id, r.reviewer_name, " +
                     "r.reviewee_id, r.reviewee_name, r.reviewer_type, r.rating, r.content, " +
                     "r.tags, r.product_name, r.product_image, r.create_time, r.update_time " +
                     "FROM review r " +
                     "WHERE r.reviewee_id = ? ORDER BY r.create_time DESC";
        return jdbcTemplate.query(sql, new FullReviewRowMapper(), userId);
    }

    // 获取用户给出的所有评价（完整信息）
    public List<Review> findGivenReviews(Long userId) {
        String sql = "SELECT r.id, r.trade_id, r.trade_no, r.reviewer_id, r.reviewer_name, " +
                     "r.reviewee_id, r.reviewee_name, r.reviewer_type, r.rating, r.content, " +
                     "r.tags, r.product_name, r.product_image, r.create_time, r.update_time " +
                     "FROM review r " +
                     "WHERE r.reviewer_id = ? ORDER BY r.create_time DESC";
        return jdbcTemplate.query(sql, new FullReviewRowMapper(), userId);
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

    // 计算用户给出的平均评分
    public double getGivenAverageRating(Long userId) {
        String sql = "SELECT AVG(rating) FROM review WHERE reviewer_id = ?";
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

    // 完整行映射器（包含交易编号、商品名称、商品图片）
    private static class FullReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review();
            review.setId(rs.getLong("id"));
            review.setTradeId(rs.getLong("trade_id"));
            review.setTradeNo(rs.getString("trade_no"));
            review.setReviewerId(rs.getLong("reviewer_id"));
            review.setReviewerName(rs.getString("reviewer_name"));
            review.setRevieweeId(rs.getLong("reviewee_id"));
            review.setRevieweeName(rs.getString("reviewee_name"));
            review.setReviewerType(rs.getInt("reviewer_type"));
            review.setRating(rs.getInt("rating"));
            review.setContent(rs.getString("content"));
            review.setTags(rs.getString("tags"));
            review.setProductName(rs.getString("product_name"));
            review.setProductImage(rs.getString("product_image"));
            review.setCreateTime(rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toLocalDateTime() : null);
            review.setUpdateTime(rs.getTimestamp("update_time") != null ? rs.getTimestamp("update_time").toLocalDateTime() : null);
            return review;
        }
    }
}
