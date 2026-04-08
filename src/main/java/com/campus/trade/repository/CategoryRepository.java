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

    public List<Category> findAllActive() {
        String sql = "SELECT * FROM category WHERE status = 1 ORDER BY sort_order DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }
}