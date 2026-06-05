package com.campus.trade.repository;

import com.campus.trade.dto.response.OrderDetailResponse;
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
import java.util.List;
import java.util.Objects;

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int updateStatusWithAuth(Long orderId, Integer status, String identityColumn, Long userId) {
        String sql = "UPDATE `order` SET status = ? WHERE id = ? AND " + identityColumn + " = ?";
        return jdbcTemplate.update(sql, status, orderId, userId);
    }

    public List<OrderDetailResponse> findBuyerOrders(Long buyerId, Integer status) {
        String sql = "SELECT o.*, p.name as product_name, p.cover_image as product_image " +
                "FROM `order` o JOIN product p ON o.product_id = p.id " +
                "WHERE o.buyer_id = ? " +
                (status != null ? "AND o.status = ? " : "") +
                "ORDER BY o.create_time DESC";

        if (status != null) {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDetailResponse.class), buyerId, status);
        }
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDetailResponse.class), buyerId);
    }

    public List<OrderDetailResponse> findSellerOrders(Long sellerId, Integer status) {
        String sql = "SELECT o.*, p.name as product_name, p.cover_image as product_image " +
                "FROM `order` o JOIN product p ON o.product_id = p.id " +
                "WHERE o.seller_id = ? " +
                (status != null ? "AND o.status = ? " : "") +
                "ORDER BY o.create_time DESC";

        if (status != null) {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDetailResponse.class), sellerId, status);
        }
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDetailResponse.class), sellerId);
    }

    // 返回值改为 Long，内部对应的 ID 设置改为 setLong() / setObject()
    public Long insert(Order order) {
        String sql = "INSERT INTO `order`(order_no, buyer_id, seller_id, product_id, product_name, " +
                "product_image, product_price, quantity, total_amount, address_id, " +
                "trade_type, meeting_time, meeting_location, status) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getOrderNo());

            // 下面这三个必定有值的主键改为 setLong
            ps.setLong(2, order.getBuyerId());
            ps.setLong(3, order.getSellerId());
            ps.setLong(4, order.getProductId());

            ps.setString(5, order.getProductName());
            ps.setString(6, order.getProductImage());
            ps.setBigDecimal(7, order.getProductPrice());
            ps.setInt(8, order.getQuantity()); // 数量仍为 Integer
            ps.setBigDecimal(9, order.getTotalAmount());

            // 极其重要：因为线下交易 addressId 可以为空(null)
            // 如果用 setLong 遇到 null 会报空指针，所以用 setObject 处理可空的 Long
            ps.setObject(10, order.getAddressId());

            ps.setInt(11, order.getTradeType()); // 交易类型仍为 Integer
            ps.setObject(12, order.getMeetingTime());
            ps.setString(13, order.getMeetingLocation());
            ps.setInt(14, order.getStatus()); // 状态仍为 Integer
            return ps;
        }, keyHolder);

        // 返回生成的 ID 时使用 .longValue()
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Order findById(Long orderId) {
        String sql = "SELECT * FROM `order` WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Order.class), orderId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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

    // 将 orderId 改为 Long
    public OrderDetailResponse findOrderDetailById(Long orderId) {
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