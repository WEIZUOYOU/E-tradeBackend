package com.campus.trade.repository;

import com.campus.trade.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 插入消息记录
     */
    public int insert(Message msg) {
        String sql = "INSERT INTO message (sender_id, receiver_id, product_id, content, type, is_read) VALUES (?, ?, ?, ?, ?, 0)";
        return jdbcTemplate.update(sql, 
                msg.getSenderId(), 
                msg.getReceiverId(), 
                msg.getProductId(), 
                msg.getContent(), 
                msg.getType());
    }

    /**
     * 获取两个用户之间的聊天记录（按时间升序）
     */
    public List<Message> findChatHistory(Integer userA, Integer userB) {
        String sql = "SELECT * FROM message WHERE (sender_id = ? AND receiver_id = ?) " +
                     "OR (sender_id = ? AND receiver_id = ?) ORDER BY create_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), userA, userB, userB, userA);
    }

    /**
     * 将未读消息标为已读
     */
    public void markAsRead(Integer readerId, Integer senderId) {
        String sql = "UPDATE message SET is_read = 1 WHERE receiver_id = ? AND sender_id = ? AND is_read = 0";
        jdbcTemplate.update(sql, readerId, senderId);
    }
}