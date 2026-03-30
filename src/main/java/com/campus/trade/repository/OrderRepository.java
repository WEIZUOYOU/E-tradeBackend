package com.campus.trade.repository;

import com.campus.trade.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Objects;

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long insert(Order order) {
        String sql = "INSERT INTO `order`(order_no, buyer_id, seller_id, product_id, product_name, product_price_at_order, quantity, total_amount, status, create_time) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, order.getOrderNo());
            ps.setLong(2, order.getBuyerId());
            ps.setLong(3, order.getSellerId());
            ps.setLong(4, order.getProductId());
            ps.setString(5, order.getProductName());
            ps.setBigDecimal(6, order.getProductPriceAtOrder());
            ps.setInt(7, order.getQuantity());
            ps.setBigDecimal(8, order.getTotalAmount());
            ps.setInt(9, order.getStatus());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
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
}
