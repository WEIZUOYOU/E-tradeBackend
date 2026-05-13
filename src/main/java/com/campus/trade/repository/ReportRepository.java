package com.campus.trade.repository;

import com.campus.trade.dto.response.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
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
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    // 更新举报状态
    public int updateStatus(Long reportId, Integer status) {
        String sql = "UPDATE report SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, reportId);
    }

    // 处理举报（记录处理人、结果）
    public int handleReport(Long reportId, Integer status, Long handlerId, String result) {
        String sql = "UPDATE report SET status = ?, handler_id = ?, handle_result = ?, handle_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, status, handlerId, result, reportId);
    }

    // 检查用户是否已举报过该商品
    public boolean exists(Long reporterId, Long productId) {
        String sql = "SELECT COUNT(*) FROM report WHERE reporter_id = ? AND product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reporterId, productId);
        return count != null && count > 0;
    }

    // 根据ID查询举报
    public ReportResponse findById(Long id) {
        String sql = "SELECT r.id, r.product_id, p.name AS productName, r.reporter_id, u.username AS reporterName, " +
                "r.reason, r.status, r.create_time " +
                "FROM report r " +
                "LEFT JOIN product p ON r.product_id = p.id " +
                "LEFT JOIN user u ON r.reporter_id = u.id " +
                "WHERE r.id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ReportResponse.class), id)
                .stream().findFirst().orElse(null);
    }

    // 分页查询全部举报
    public List<ReportResponse> findAll(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT r.id, r.product_id, p.name AS productName, r.reporter_id, u.username AS reporterName, " +
                "r.reason, r.status, r.create_time " +
                "FROM report r " +
                "LEFT JOIN product p ON r.product_id = p.id " +
                "LEFT JOIN user u ON r.reporter_id = u.id " +
                "ORDER BY r.create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ReportResponse.class), size, offset);
    }

    // 按状态分页查询举报
    public List<ReportResponse> findByStatus(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT r.id, r.product_id, p.name AS productName, r.reporter_id, u.username AS reporterName, " +
                "r.reason, r.status, r.create_time " +
                "FROM report r " +
                "LEFT JOIN product p ON r.product_id = p.id " +
                "LEFT JOIN user u ON r.reporter_id = u.id " +
                "WHERE r.status = ? " +
                "ORDER BY r.create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ReportResponse.class), status, size, offset);
    }
}
