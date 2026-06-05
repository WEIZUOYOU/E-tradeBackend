package com.campus.trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Trade {
    private Long id;
    private String tradeNo;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
    
    // 买家信息
    private String buyerName;
    private String buyerAvatar;
    private Integer buyerCreditScore;
    private Integer buyerIsAuth;
    private String buyerPhone;
    
    // 卖家信息
    private String sellerName;
    private String sellerAvatar;
    private Integer sellerCreditScore;
    private Integer sellerIsAuth;
    private String sellerPhone;
    
    private String meetingLocation;
    private LocalDateTime meetingTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}