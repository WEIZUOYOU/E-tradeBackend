package com.campus.trade.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Objects;

@Repository
public class ReportRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 提交举报
    public Long insert(Long reporterId, Long productId, String reason) {
        String sql = "INSERT INTO report(reporter_id, product_id, reason, status) VALUES(?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, reporterId);
            ps.setLong(2, productId);
            ps.setString(3, reason);
            return ps;
        }, keyHolder);
        // 后台管理暂未实现，因此此处未实现提醒功能，后续可根据需要添加
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    // 更新举报状态
    public int updateStatus(Long reportId, Integer status) {
        String sql = "UPDATE report SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, reportId);
    }

    // 检查用户是否已举报过该商品
    public boolean exists(Long reporterId, Long productId) {
        String sql = "SELECT COUNT(*) FROM report WHERE reporter_id = ? AND product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reporterId, productId);
        return count != null && count > 0;
    }
}
