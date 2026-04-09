package com.campus.trade.service;

import com.campus.trade.dto.CreateOrderRequest;
import com.campus.trade.dto.OrderDetailResponse;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.Product;
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

    @Transactional
    public Order createOrder(Integer buyerId, CreateOrderRequest req) {
        // 1. 查询商品并校验状态（SQL中 1 为上架）
        Product product = productRepository.findById(req.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 1) { 
            throw new BusinessException("商品已下架或售罄");
        }
        if (product.getStock() < req.getQuantity()) {
            throw new BusinessException("库存不足");
        }
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 2. 线下交易特有逻辑校验
        if (req.getTradeType() == 1) { // 1 代表线下
            if (req.getMeetingTime() == null || req.getMeetingLocation() == null) {
                throw new BusinessException("线下交易请填写约定的时间与地点");
            }
        }

        // 3. 扣减库存
        int rows = productRepository.deductStock(req.getProductId(), req.getQuantity());
        if (rows == 0) {
            throw new BusinessException("系统繁忙，扣减库存失败");
        }

        // 4. 计算总金额与生成订单
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductImage(product.getCoverImage()); // 存储快照图片
        order.setProductPrice(product.getPrice());      // 存储快照价格
        order.setQuantity(req.getQuantity());
        order.setTotalAmount(total);
        order.setAddressId(req.getAddressId());
        
        // 线下交易字段赋值
        order.setTradeType(req.getTradeType());
        order.setMeetingTime(req.getMeetingTime());
        order.setMeetingLocation(req.getMeetingLocation());
        
        order.setStatus(0); // 0-待支付/待确认
        
        orderRepository.insert(order);
        return order;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = ORDER_SEQ.incrementAndGet() % 10000;
        return timestamp + String.format("%04d", seq);
    }

    public OrderDetailResponse getOrderDetail(Integer orderId, Integer currentUserId) {
        OrderDetailResponse orderDetail = orderRepository.findOrderDetailById(orderId);
        if (orderDetail == null) {
            throw new BusinessException("订单不存在");
        }
        if (!orderDetail.getBuyerId().equals(currentUserId) && !orderDetail.getSellerId().equals(currentUserId)) {
            throw new BusinessException("无权查看该订单");
        }
        return orderDetail;
    }
}