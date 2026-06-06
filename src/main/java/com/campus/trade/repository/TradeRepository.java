package com.campus.trade.repository;

import com.campus.trade.entity.Trade;
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
public class TradeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 插入交易记录（返回带主键的Trade）
     */
    public int insert(Trade trade) {
        String sql = "INSERT INTO trade (trade_no, product_id, buyer_id, seller_id, product_name, " +
                "product_price, product_image, buyer_name, buyer_avatar, buyer_credit_score, " +
                "buyer_is_auth, buyer_phone, seller_name, seller_avatar, seller_credit_score, " +
                "seller_is_auth, seller_phone, meeting_location, meeting_time, status, create_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, trade.getTradeNo());
            ps.setObject(2, trade.getProductId());
            ps.setObject(3, trade.getBuyerId());
            ps.setObject(4, trade.getSellerId());
            ps.setString(5, trade.getProductName());
            ps.setBigDecimal(6, trade.getProductPrice());
            ps.setString(7, trade.getProductImage());
            ps.setString(8, trade.getBuyerName());
            ps.setString(9, trade.getBuyerAvatar());
            ps.setObject(10, trade.getBuyerCreditScore());
            ps.setObject(11, trade.getBuyerIsAuth());
            ps.setString(12, trade.getBuyerPhone());
            ps.setString(13, trade.getSellerName());
            ps.setString(14, trade.getSellerAvatar());
            ps.setObject(15, trade.getSellerCreditScore());
            ps.setObject(16, trade.getSellerIsAuth());
            ps.setString(17, trade.getSellerPhone());
            ps.setString(18, trade.getMeetingLocation());
            ps.setObject(19, trade.getMeetingTime());
            ps.setObject(20, trade.getStatus());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            trade.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        }
        return 1;
    }

    /**
     * 根据ID查询交易
     */
    public Trade findById(Long id) {
        String sql = "SELECT * FROM trade WHERE id = ?";
        List<Trade> trades = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Trade.class), id);
        return trades.isEmpty() ? null : trades.get(0);
    }

    /**
     * 卖家确认交易（条件更新，保证幂等性）
     */
    public int updateSellerConfirm(Long tradeId, Long sellerId, String sellerPhone) {
        String sql = "UPDATE trade SET seller_phone = ?, status = 1, " +
                "update_time = NOW() " +
                "WHERE id = ? AND seller_id = ? AND status = 0";
        return jdbcTemplate.update(sql, sellerPhone, tradeId, sellerId);
    }

    /**
     * 修改交易信息（条件更新 + 记录修改人）
     */
    public int updateTradeInfo(Long tradeId, Long userId, String meetingLocation,
                               java.time.LocalDateTime meetingTime,
                               Long updaterId, String updaterRole) {
        String sql = "UPDATE trade SET meeting_location = ?, meeting_time = ?, " +
                "update_time = NOW() " +
                "WHERE id = ? AND (buyer_id = ? OR seller_id = ?) AND status = 1";
        return jdbcTemplate.update(sql, meetingLocation, meetingTime, tradeId, userId, userId);
    }

    /**
     * 清除修改标记
     */
    public int clearUpdateFlag(Long tradeId) {
        String sql = "UPDATE trade SET update_time = NOW() WHERE id = ? AND status = 1";
        return jdbcTemplate.update(sql, tradeId);
    }

    /**
     * 更新交易状态
     */
    public int updateStatus(Long tradeId, int status) {
        String sql = "UPDATE trade SET status = ?, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, status, tradeId);
    }

    /**
     * 完成交易
     */
    public int completeTrade(Long tradeId, Long userId) {
        String sql = "UPDATE trade SET status = 4, update_time = NOW() " +
                "WHERE id = ? AND (buyer_id = ? OR seller_id = ?) AND status = 1";
        return jdbcTemplate.update(sql, tradeId, userId, userId);
    }

    /**
     * 取消交易
     */
    public int cancelTrade(Long tradeId, Long userId) {
        String sql = "UPDATE trade SET status = 5, update_time = NOW() " +
                "WHERE id = ? AND (buyer_id = ? OR seller_id = ?) AND status = 0";
        return jdbcTemplate.update(sql, tradeId, userId, userId);
    }

    /**
     * 查询我的交易列表（分页）
     */
    public List<Trade> findMyTrades(Long userId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        String sql;
        Object[] args;
        if (status != null) {
            sql = "SELECT * FROM trade WHERE (buyer_id = ? OR seller_id = ?) AND status = ? " +
                    "ORDER BY create_time DESC LIMIT ? OFFSET ?";
            args = new Object[] { userId, userId, status, size, offset };
        } else {
            sql = "SELECT * FROM trade WHERE buyer_id = ? OR seller_id = ? " +
                    "ORDER BY create_time DESC LIMIT ? OFFSET ?";
            args = new Object[] { userId, userId, size, offset };
        }
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Trade.class), args);
    }

    /**
     * 统计我的交易数量
     */
    public int countMyTrades(Long userId, Integer status) {
        String sql;
        Object[] args;
        if (status != null) {
            sql = "SELECT COUNT(*) FROM trade WHERE (buyer_id = ? OR seller_id = ?) AND status = ?";
            args = new Object[] { userId, userId, status };
        } else {
            sql = "SELECT COUNT(*) FROM trade WHERE buyer_id = ? OR seller_id = ?";
            args = new Object[] { userId, userId };
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return count == null ? 0 : count;
    }
}