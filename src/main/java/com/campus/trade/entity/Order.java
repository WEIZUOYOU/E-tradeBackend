package com.campus.trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Integer id;
    private String orderNo;
    private Integer buyerId;
    private Integer sellerId;
    private Integer productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice; // 对应数据库 product_price
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer addressId;        // 关联地址 (新增)
    
    // 线下交易相关字段 (新增)
    private Integer tradeType;        // 0-线上支付/快递，1-线下交易
    private LocalDateTime meetingTime;
    private String meetingLocation;
    
    private Integer status;           // 0-待支付/确认, 1-已支付/交易中, 2-已发货/交付, 3-已完成, 4-已取消
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliverTime; // 发货/交付时间
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String remark;            // 订单备注
}