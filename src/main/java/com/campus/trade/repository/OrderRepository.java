package com.campus.trade.repository;

import com.campus.trade.dto.OrderDetailResponse;
import com.campus.trade.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer insert(Order order) {
        // 对齐 SQL Schema 中的字段名
        String sql = "INSERT INTO `order`(order_no, buyer_id, seller_id, product_id, product_name, " +
                     "product_image, product_price, quantity, total_amount, address_id, " +
                     "trade_type, meeting_time, meeting_location, status) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getOrderNo());
            ps.setInt(2, order.getBuyerId());
            ps.setInt(3, order.getSellerId());
            ps.setInt(4, order.getProductId());
            ps.setString(5, order.getProductName());
            ps.setString(6, order.getProductImage());
            ps.setBigDecimal(7, order.getProductPrice());
            ps.setInt(8, order.getQuantity());
            ps.setBigDecimal(9, order.getTotalAmount());
            ps.setInt(10, order.getAddressId());
            ps.setInt(11, order.getTradeType());
            ps.setObject(12, order.getMeetingTime()); // LocalDateTime 直接映射
            ps.setString(13, order.getMeetingLocation());
            ps.setInt(14, order.getStatus());
            return ps;
        }, keyHolder);
        
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Order findByOrderNo(String orderNo) {
        String sql = "SELECT * FROM `order` WHERE order_no = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), orderNo)
                .stream().findFirst().orElse(null);
    }

    public int updateStatus(String orderNo, Integer status) {
        String sql = "UPDATE `order` SET status = ? WHERE order_no = ?";
        return jdbcTemplate.update(sql, status, orderNo);
    }

    public OrderDetailResponse findOrderDetailById(Integer orderId) {
        // 根据最新的 Table Join 逻辑更新
        String sql = "SELECT o.*, " +
                     "seller.username AS seller_name, seller.avatar AS seller_avatar, " +
                     "buyer.username AS buyer_name, buyer.avatar AS buyer_avatar, " +
                     "a.receiver_name, a.receiver_phone, a.detail_address AS receiver_address " +
                     "FROM `order` o " +
                     "LEFT JOIN user seller ON o.seller_id = seller.id " +
                     "LEFT JOIN user buyer ON o.buyer_id = buyer.id " +
                     "LEFT JOIN address a ON o.address_id = a.id " +
                     "WHERE o.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(OrderDetailResponse.class), orderId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}