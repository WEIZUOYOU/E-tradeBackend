package com.campus.trade.service;

import com.campus.trade.dto.CreateOrderRequest;
import com.campus.trade.dto.OrderDetailResponse;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.OrderRepository;
import com.campus.trade.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(0);

    // 创建订单（简化版，无支付）
    @Transactional
    public Order createOrder(Long buyerId, CreateOrderRequest req) {
        // 1. 查询商品
        Product product = productRepository.findById(req.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 0) {
            throw new BusinessException("商品已下架或售出");
        }
        if (product.getStock() < req.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        // 2. 扣减库存
        int rows = productRepository.deductStock(req.getProductId(), req.getQuantity());
        if (rows == 0) {
            throw new BusinessException("扣减库存失败，请重试");
        }

        // 3. 生成订单号
        String orderNo = generateOrderNo();

        // 4. 计算总金额
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));

        // 5. 保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductPriceAtOrder(product.getPrice());
        order.setQuantity(req.getQuantity());
        order.setTotalAmount(total);
        order.setStatus(0); // 待支付（实际项目可跳过支付直接设置为已完成，取决于业务）
        orderRepository.insert(order);

        return order;
    }

    // 简单生成订单号：yyyyMMddHHmmss + 用户ID后4位 + 自增序列
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = ORDER_SEQ.incrementAndGet() % 10000;
        return timestamp + String.format("%04d", seq);
    }

    public OrderDetailResponse getOrderDetail(Long orderId, Long currentUserId) {
        // 1. 查询订单详情
        OrderDetailResponse orderDetail = orderRepository.findOrderDetailById(orderId);
        if (orderDetail == null) {
            throw new BusinessException("订单不存在");
        }
        // 2. 权限校验：只有买家或卖家可以查看订单详情（管理员暂不考虑，可根据需要扩展）
        if (!orderDetail.getBuyerId().equals(currentUserId) && !orderDetail.getSellerId().equals(currentUserId)) {
            throw new BusinessException("无权查看该订单");
        }
        return orderDetail;
    }
}
