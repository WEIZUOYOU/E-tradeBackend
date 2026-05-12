package com.campus.trade.repository;

import com.campus.trade.entity.Favorite;
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
public class FavoriteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 添加收藏
    public Long insert(Long userId, Long productId) {
        String sql = "INSERT INTO favorite(user_id, product_id) VALUES(?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    // 取消收藏
    public int delete(Long userId, Long productId) {
        String sql = "DELETE FROM favorite WHERE user_id = ? AND product_id = ?";
        return jdbcTemplate.update(sql, userId, productId);
    }

    // 检查是否已收藏
    public boolean exists(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM favorite WHERE user_id = ? AND product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    // 查询用户的收藏列表（分页）
    public List<Favorite> findByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM favorite WHERE user_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Favorite.class), userId, size, offset);
    }

    // 查询用户收藏的商品数量
    public Integer countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM favorite WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }
}
