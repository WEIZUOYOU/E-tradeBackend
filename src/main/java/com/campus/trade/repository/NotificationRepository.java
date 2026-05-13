package com.campus.trade.repository;

import com.campus.trade.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 插入通知
    public void insert(Notification notification) {
        String sql = "INSERT INTO notification(user_id, type, title, content, related_id, is_read, create_time) VALUES(?, ?, ?, ?, ?, 0, NOW())";
        jdbcTemplate.update(sql,
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedId());
    }

    // 分页查询用户通知
    public List<Notification> findByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Notification.class), userId, size, offset);
    }

    // 获取未读数量
    public int getUnreadCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // 标记单条已读
    public int markAsRead(Long notificationId, Long userId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, notificationId, userId);
    }

    // 标记全部已读
    public int markAllAsRead(Long userId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        return jdbcTemplate.update(sql, userId);
    }
}
