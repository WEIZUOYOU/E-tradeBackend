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

    public OrderDetailResponse findOrderDetailById(Long orderId) {
        String sql = "SELECT o.id, o.order_no, o.status, o.quantity, o.total_amount, o.create_time, o.pay_time, o.complete_time, " +
             "o.product_id, o.product_name, o.product_price_at_order, " +
             "SUBSTRING_INDEX(p.image_urls, ',', 1) AS product_image, " +   // 取第一张图作为主图
             "seller.id AS seller_id, seller.username AS seller_name, seller.avatar AS seller_avatar, " +
             "buyer.id AS buyer_id, buyer.username AS buyer_name, buyer.avatar AS buyer_avatar, " +
             "a.contact AS receiver_name, a.phone AS receiver_phone, a.detail_address AS receiver_address, " +
             "o.logistics_company, o.tracking_no " +
             "FROM `order` o " +
             "LEFT JOIN product p ON o.product_id = p.id " +
             "LEFT JOIN user seller ON o.seller_id = seller.id " +
             "LEFT JOIN user buyer ON o.buyer_id = buyer.id " +
             "LEFT JOIN address a ON o.address_id = a.id " +
             "WHERE o.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                OrderDetailResponse dto = new OrderDetailResponse();
                dto.setId(rs.getLong("id"));
                dto.setOrderNo(rs.getString("order_no"));
                dto.setStatus(rs.getInt("status"));
                dto.setQuantity(rs.getInt("quantity"));
                dto.setTotalAmount(rs.getBigDecimal("total_amount"));
                dto.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
                dto.setPayTime(rs.getTimestamp("pay_time") != null ? rs.getTimestamp("pay_time").toLocalDateTime() : null);
                dto.setCompleteTime(rs.getTimestamp("complete_time") != null ? rs.getTimestamp("complete_time").toLocalDateTime() : null);

                dto.setProductId(rs.getLong("product_id"));
                dto.setProductName(rs.getString("product_name"));
                dto.setProductPriceAtOrder(rs.getBigDecimal("product_price_at_order"));
                dto.setProductImage(rs.getString("product_image"));

                dto.setSellerId(rs.getLong("seller_id"));
                dto.setSellerName(rs.getString("seller_name"));
                dto.setSellerAvatar(rs.getString("seller_avatar"));

                dto.setBuyerId(rs.getLong("buyer_id"));
                dto.setBuyerName(rs.getString("buyer_name"));
                dto.setBuyerAvatar(rs.getString("buyer_avatar"));

                dto.setReceiverName(rs.getString("receiver_name"));
                dto.setReceiverPhone(rs.getString("receiver_phone"));
                dto.setReceiverAddress(rs.getString("receiver_address"));

                dto.setLogisticsCompany(rs.getString("logistics_company"));
                dto.setTrackingNo(rs.getString("tracking_no"));
                return dto;
            }, orderId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
