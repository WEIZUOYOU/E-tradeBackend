package com.campus.trade.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer reviewType;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
