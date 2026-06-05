package com.campus.trade.service;

import com.campus.trade.dto.request.CreateOrderRequest;
import com.campus.trade.dto.response.OrderDetailResponse;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.Product;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.OrderRepository;
import com.campus.trade.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    // 在 OrderService 类中添加注入
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ProductRepository productRepository;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(0);

    public List<OrderDetailResponse> getBuyerOrders(Long buyerId, Integer status) {
        return orderRepository.findBuyerOrders(buyerId, status);
    }

    public List<OrderDetailResponse> getSellerOrders(Long sellerId, Integer status) {
        return orderRepository.findSellerOrders(sellerId, status);
    }

    // 买家取消订单（仅限待卖家确认状态）
    @Transactional
    public void cancelOrderByBuyer(Long orderId, Long buyerId) {
        String sql = "UPDATE `order` SET status = 5 WHERE id = ? AND buyer_id = ? AND status = 0";
        int rows = jdbcTemplate.update(sql, orderId, buyerId);
        if (rows == 0)
            throw new BusinessException("交易不可取消或无权操作");
    }

    // 卖家确认交易（填写联系电话后确认）：0 -> 1
    @Transactional
    public void confirmTrade(Long tradeId, Long sellerId, String contactPhone) {
        // 检查交易状态是否为待卖家确认
        Order order = orderRepository.findById(tradeId);
        if (order == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(3005, "无操作权限");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(3006, "交易状态不允许此操作");
        }
        
        // 更新状态为交易中并保存联系电话
        String sql = "UPDATE `order` SET status = 1, contact_phone = ? WHERE id = ? AND seller_id = ?";
        int rows = jdbcTemplate.update(sql, contactPhone, tradeId, sellerId);
        if (rows == 0)
            throw new BusinessException("交易确认失败，请检查状态");
    }

    // 修改交易信息（交易中状态可修改地点和时间）：1 -> 1
    @Transactional
    public void updateTrade(Long tradeId, Long userId, LocalDateTime meetingTime, String meetingLocation) {
        Order order = orderRepository.findById(tradeId);
        if (order == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(3005, "无操作权限");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(3006, "只有交易中状态可以修改信息");
        }
        
        // 更新交易时间和地点
        String sql = "UPDATE `order` SET meeting_time = ?, meeting_location = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, meetingTime, meetingLocation, tradeId);
        if (rows == 0)
            throw new BusinessException("修改失败");
    }

    // 卖家标记交付：1 -> 2
    @Transactional
    public void markDelivered(Long tradeId, Long sellerId) {
        Order order = orderRepository.findById(tradeId);
        if (order == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(3005, "无操作权限");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(3006, "交易状态不允许此操作");
        }
        
        int rows = orderRepository.updateStatusWithAuth(tradeId, 2, "seller_id", sellerId);
        if (rows == 0)
            throw new BusinessException("操作失败");
    }

    // 买家确认收到：2 -> 3
    @Transactional
    public void confirmReceive(Long tradeId, Long buyerId) {
        Order order = orderRepository.findById(tradeId);
        if (order == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(3005, "无操作权限");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(3006, "交易状态不允许此操作");
        }
        
        int rows = orderRepository.updateStatusWithAuth(tradeId, 3, "buyer_id", buyerId);
        if (rows == 0)
            throw new BusinessException("确认收货失败");
    }

    // 卖家确认完成：3 -> 4
    @Transactional
    public void confirmComplete(Long tradeId, Long sellerId) {
        Order order = orderRepository.findById(tradeId);
        if (order == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(3005, "无操作权限");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException(3006, "交易状态不允许此操作");
        }
        
        int rows = orderRepository.updateStatusWithAuth(tradeId, 4, "seller_id", sellerId);
        if (rows == 0)
            throw new BusinessException("确认完成失败");

        // 进阶逻辑：可以在此处更新卖家 credit_score 或 trade_count
    }

    // 获取我的交易列表（作为买家或卖家）
    public List<OrderDetailResponse> getMyTrades(Long userId, Integer status) {
        List<OrderDetailResponse> buyerOrders = orderRepository.findBuyerOrders(userId, status);
        List<OrderDetailResponse> sellerOrders = orderRepository.findSellerOrders(userId, status);
        
        // 合并买家和卖家的订单
        buyerOrders.addAll(sellerOrders);
        return buyerOrders;
    }

    @Transactional
    public Order createOrder(Long buyerId, CreateOrderRequest req) {
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
            throw new BusinessException(3001, "不能购买自己的商品");
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
            throw new BusinessException(3002, "商品库存不足");
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
        order.setProductPrice(product.getPrice()); // 存储快照价格
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

    public OrderDetailResponse getOrderDetail(Long orderId, Long currentUserId) {
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