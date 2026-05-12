package com.campus.trade.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long reporterId;
    private String reporterName;
    private String reason;
    private Integer status; // 0-未处理, 1-已处理, 2-已驳回
    private LocalDateTime createTime;
}
