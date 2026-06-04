package com.campus.trade.repository;

import com.campus.trade.entity.Category;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询所有启用的一级分类，按sort_order升序排列
     */
    public List<Category> findActiveCategories() {
        String sql = "SELECT id, name FROM category WHERE parent_id IS NULL AND is_active = 1 ORDER BY sort_order ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }

    /**
     * 根据ID查询分类
     */
    public Category findById(Long id) {
        String sql = "SELECT * FROM category WHERE id = ?";
        List<Category> categories = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class), id);
        return categories.isEmpty() ? null : categories.get(0);
    }
}