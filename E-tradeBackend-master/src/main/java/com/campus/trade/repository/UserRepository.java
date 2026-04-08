package com.campus.trade.repository;

import com.campus.trade.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Objects;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByStudentId(String studentId) {
        String sql = "SELECT * FROM user WHERE student_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), studentId)
                .stream().findFirst().orElse(null);
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), id)
                .stream().findFirst().orElse(null);
    }

    public Long insert(User user) {
        String sql = "INSERT INTO user(student_id, username, password, phone, credit_score, status, create_time) " +
                "VALUES(?, ?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, user.getStudentId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone());
            ps.setInt(5, user.getCreditScore() != null ? user.getCreditScore() : 100);
            ps.setInt(6, user.getStatus() != null ? user.getStatus() : 0);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        String sql = "UPDATE user SET avatar = ? WHERE id = ?";
        jdbcTemplate.update(sql, avatarUrl, userId);
    }

    // 根据手机号查询
    public User findByPhone(String phone) {
        String sql = "SELECT * FROM user WHERE phone = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), phone)
                .stream().findFirst().orElse(null);
    }

    // 更新实名认证信息
    public void updateVerify(Long userId, String studentId, String realName, Integer status) {
        String sql = "UPDATE user SET student_id=?, real_name=?, verify_status=? WHERE id=?";
        jdbcTemplate.update(sql, studentId, realName, status, userId);
    }
}
