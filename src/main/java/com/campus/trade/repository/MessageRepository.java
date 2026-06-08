package com.campus.trade.repository;

import com.campus.trade.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 插入消息记录（处理唯一约束冲突）
     * @return 消息ID，如果因唯一约束冲突插入失败则返回 null
     */
    public Long insert(Message msg) {
        try {
            String sql = "INSERT INTO message (sender_id, receiver_id, product_id, content, type, is_read, trade_id, trade_status, trade_data) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                    msg.getSenderId(), 
                    msg.getReceiverId(), 
                    msg.getProductId(), 
                    msg.getContent(), 
                    msg.getType(),
                    msg.getTradeId(),
                    msg.getTradeStatus(),
                    msg.getTradeData());
            
            // 获取刚插入的 ID
            String sqlLastId = "SELECT LAST_INSERT_ID()";
            return jdbcTemplate.queryForObject(sqlLastId, Long.class);
        } catch (Exception e) {
            // 如果是唯一约束冲突（Duplicate entry），返回 null 而不是抛出异常
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                log.warn("消息插入失败：唯一约束冲突，senderId={}, receiverId={}, tradeId={}", 
                        msg.getSenderId(), msg.getReceiverId(), msg.getTradeId());
                return null;
            }
            throw e; // 其他异常正常抛出
        }
    }

    /**
     * 根据 ID 查询消息
     */
    public Message findById(Long id) {
        String sql = "SELECT * FROM message WHERE id = ?";
        List<Message> messages = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), id);
        return messages.isEmpty() ? null : messages.get(0);
    }

    /**
     * 获取两个用户之间的聊天记录（按时间升序）
     */
    public List<Message> findChatHistory(Long userA, Long userB) {
        String sql = "SELECT * FROM message WHERE (sender_id = ? AND receiver_id = ?) " +
                     "OR (sender_id = ? AND receiver_id = ?) ORDER BY create_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), userA, userB, userB, userA);
    }

    /**
     * 将未读消息标为已读
     */
    public void markAsRead(Long readerId, Long senderId) {
        String sql = "UPDATE message SET is_read = 1 WHERE receiver_id = ? AND sender_id = ? AND is_read = 0";
        jdbcTemplate.update(sql, readerId, senderId);
    }

    /**
     * 获取用户的所有会话列表（每个会话最新一条消息）
     */
    public List<Message> findSessions(Long userId) {
        String sql = "SELECT m.* FROM message m " +
                     "INNER JOIN (" +
                     "    SELECT CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END AS other_id, " +
                     "           MAX(create_time) AS last_time " +
                     "    FROM message " +
                     "    WHERE sender_id = ? OR receiver_id = ? " +
                     "    GROUP BY other_id" +
                     ") t ON (CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END = t.other_id AND m.create_time = t.last_time) " +
                     "ORDER BY m.create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), userId, userId, userId, userId);
    }

    /**
     * 获取用户的未读消息总数
     */
    public int countUnread(Long userId) {
        String sql = "SELECT COUNT(*) FROM message WHERE receiver_id = ? AND is_read = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * 获取用户未读消息的会话数
     */
    public int countUnreadSessions(Long userId) {
        String sql = "SELECT COUNT(DISTINCT sender_id) FROM message WHERE receiver_id = ? AND is_read = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * 标记单条消息已读
     */
    public void markMessageAsRead(Long messageId, Long userId) {
        String sql = "UPDATE message SET is_read = 1 WHERE id = ? AND receiver_id = ?";
        jdbcTemplate.update(sql, messageId, userId);
    }

    /**
     * 标记与某个用户的所有消息已读
     */
    public void markSessionAsRead(Long userId, Long targetUserId) {
        String sql = "UPDATE message SET is_read = 1 WHERE receiver_id = ? AND sender_id = ? AND is_read = 0";
        jdbcTemplate.update(sql, userId, targetUserId);
    }

    /**
     * 统计与某个用户的未读消息数
     */
    public int countUnreadBySender(Long userId, Long senderId) {
        String sql = "SELECT COUNT(*) FROM message WHERE receiver_id = ? AND sender_id = ? AND is_read = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, senderId);
        return count != null ? count : 0;
    }

    /**
     * 检查是否已存在相同交易卡片消息（用于幂等性控制）
     * 同一发送者在 5 秒内对同一交易相同状态只能发送一次
     */
    public boolean existsTradeCardMessage(Long senderId, Long receiverId, Long tradeId, int tradeStatus) {
        String sql = "SELECT COUNT(*) FROM message " +
                     "WHERE sender_id = ? AND receiver_id = ? AND trade_id = ? " +
                     "AND trade_status = ? AND type = 1 " +
                     "AND create_time >= DATE_SUB(NOW(), INTERVAL 5 SECOND)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, senderId, receiverId, tradeId, tradeStatus);
        return count != null && count > 0;
    }
}