package com.campus.trade.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewHistoryResponse {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Integer reviewType; // 0-买家评卖家, 1-卖家评买家
    private Integer rating; // 1-5星
    private String content; // 评价内容
    private LocalDateTime createTime;
}
