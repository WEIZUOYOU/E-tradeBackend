package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderReview {
    private Long id;
    private Long orderId;
    private Long reviewerId; // 评价者ID
    private Long revieweeId; // 被评价者ID
    private Integer reviewType; // 0-买家评卖家，1-卖家评买家
    private Integer rating; // 1-5星
    private String content; // 评价内容
    private LocalDateTime createTime;
}
