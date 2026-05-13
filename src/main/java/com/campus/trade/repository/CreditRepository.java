package com.campus.trade.repository;

import com.campus.trade.entity.UserCredit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class CreditRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 查询用户信用档案
    public UserCredit findByUserId(Long userId) {
        String sql = "SELECT * FROM user_credit WHERE user_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserCredit.class), userId)
                .stream().findFirst().orElse(null);
    }

    // 初始化用户信用档案
    public void insert(Long userId) {
        String sql = "INSERT INTO user_credit(user_id, credit_score, trade_count, good_review_rate) " +
                "VALUES(?, 100, 0, 0.00)";
        jdbcTemplate.update(sql, userId);
    }

    // 更新交易次数
    public int updateTradeCount(Long userId, Integer tradeCount) {
        String sql = "UPDATE user_credit SET trade_count = ? WHERE user_id = ?";
        return jdbcTemplate.update(sql, tradeCount, userId);
    }

    // 更新好评率
    public int updateGoodReviewRate(Long userId, BigDecimal rate) {
        String sql = "UPDATE user_credit SET good_review_rate = ? WHERE user_id = ?";
        return jdbcTemplate.update(sql, rate, userId);
    }

    // 查询用户收到的评价总数
    public Integer getReviewCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM order_review WHERE reviewee_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    // 查询用户好评数（4-5星）
    public Integer getGoodReviewCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM order_review WHERE reviewee_id = ? AND rating >= 4";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    // 查询用户已完成的交易次数（买家）
    public Integer getBuyerTradeCount(Long buyerId) {
        String sql = "SELECT COUNT(*) FROM `order` WHERE buyer_id = ? AND status = 3";
        return jdbcTemplate.queryForObject(sql, Integer.class, buyerId);
    }

    // 查询用户已完成的交易次数（卖家）
    public Integer getSellerTradeCount(Long sellerId) {
        String sql = "SELECT COUNT(*) FROM `order` WHERE seller_id = ? AND status = 3";
        return jdbcTemplate.queryForObject(sql, Integer.class, sellerId);
    }
}
