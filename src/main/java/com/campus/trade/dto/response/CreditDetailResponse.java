package com.campus.trade.dto.response;

import lombok.Data;

@Data
public class CreditDetailResponse {
    private Long userId;
    private String username;
    private String avatar;
    private Integer creditScore; // 信用分
    private Integer tradeCount; // 交易次数
    private Double goodReviewRate; // 好评率 (0-100%)
    private Integer totalReviews; // 总评价数
    private Integer goodReviews; // 好评数
}
