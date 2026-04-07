package com.campus.trade.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SchoolUserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean exists(String studentId, String realName) {
        String sql = "SELECT COUNT(*) FROM school_user WHERE student_id = ? AND real_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, realName);
        return count != null && count > 0;
    }
}