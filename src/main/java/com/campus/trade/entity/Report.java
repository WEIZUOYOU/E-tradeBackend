package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Report {
    private Long id;
    private Long reporterId;
    private Long productId;
    private String reason;
    private Integer status; // 0-未处理, 1-已处理, 2-已驳回
    private LocalDateTime createTime;
}
