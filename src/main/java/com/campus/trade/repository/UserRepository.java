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
import java.util.List;

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
        String sql = "UPDATE user SET avatar = ?, update_time = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, avatarUrl, userId);
    }

    public int updateUsername(Long userId, String username) {
        String sql = "UPDATE user SET username = ?, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, username, userId);
    }

    // 根据手机号查询
    public User findByPhone(String phone) {
        String sql = "SELECT * FROM user WHERE phone = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), phone)
                .stream().findFirst().orElse(null);
    }

    // 更新实名认证信息
    // UserRepository.java
    public void updateVerify(Long userId, String studentId, String realName, Integer status) {
        // 将 verify_status 改为 is_auth
        String sql = "UPDATE user SET student_id=?, real_name=?, is_auth=? WHERE id=?";
        jdbcTemplate.update(sql, studentId, realName, status, userId);
    }

    // 更新信用分
    public void updateCreditScore(Long userId, int scoreChange) {
        String sql = "UPDATE user SET credit_score = GREATEST(0, credit_score + ?) WHERE id = ?";
        jdbcTemplate.update(sql, scoreChange, userId);
    }

    // 冻结/解冻账号
    public int updateStatus(Long userId, Integer status) {
        String sql = "UPDATE user SET status = ?, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, status, userId);
    }

    // 重置密码
    public int updatePassword(Long userId, String encodedPassword) {
        String sql = "UPDATE user SET password = ?, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, encodedPassword, userId);
    }

    // 分页查询全部用户
    public List<User> findAll(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM user ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), size, offset);
    }

    // 按状态分页查询用户
    public List<User> findByStatus(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM user WHERE status = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), status, size, offset);
    }

    // 按认证状态分页查询用户
    public List<User> findByAuthStatus(Integer isAuth, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM user WHERE is_auth = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), isAuth, size, offset);
    }
}