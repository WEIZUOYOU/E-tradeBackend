package com.campus.trade.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCredit {
    private Long userId;
    private Integer creditScore; // 信用分
    private Integer tradeCount; // 交易次数
    private BigDecimal goodReviewRate; // 好评率 (0.00-100.00)
}
