package com.campus.trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo; // 订单编号
    private Long buyerId; // 买家
    private Long sellerId; // 卖家
    private Long productId; // 商品ID (新增)
    private String productName; // 商品名称 (冗余字段，方便查询展示)
    private String productImage; // 商品图片 (冗余字段，方便查询展示)
    private BigDecimal productPrice; // 对应数据库 product_price
    private Integer quantity; // 商品数量
    private BigDecimal totalAmount; // 总金额
    private Long addressId; // 关联地址 (新增)

    // 线下交易相关字段 (新增)
    private Integer tradeType; // 0-线上支付/快递，1-线下交易
    private LocalDateTime meetingTime; // 交易时间
    private String meetingLocation; // 交易地点

    private Integer status; // 0-待支付/确认, 1-已支付/交易中, 2-已发货/交付, 3-已完成, 4-已取消
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime payTime; // 支付时间
    private LocalDateTime deliverTime; // 发货/交付时间
    private LocalDateTime completeTime; // 交易完成时间
    private LocalDateTime cancelTime; // 取消时间
    private String remark; // 订单备注
}