package com.campus.trade.dto;

import lombok.Data;

@Data
public class ReviewProductRequest {
    private String reason; // 驳回原因（驳回时必填）
}
