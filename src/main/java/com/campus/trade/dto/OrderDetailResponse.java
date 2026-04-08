package com.campus.trade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情响应DTO
 */
@Data
public class OrderDetailResponse {
    private Long id;
    private String orderNo;
    private Integer status;            // 订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;

    // 商品信息
    private Long productId;
    private String productName;
    private BigDecimal productPriceAtOrder;
    private String productImage;       // 商品主图

    // 卖家信息
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;

    // 买家信息
    private Long buyerId;
    private String buyerName;
    private String buyerAvatar;

    // 收货地址信息（如果有关联）
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    // 物流信息
    private String logisticsCompany;
    private String trackingNo;
}