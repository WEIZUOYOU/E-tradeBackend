package com.campus.trade.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "购买数量不能为空")
    private Integer quantity;

    // 🌟 修复点：去掉 @NotNull，因为线下交易时前端可能不传 addressId
    private Long addressId;

    @NotNull(message = "交易方式不能为空")
    private Integer tradeType; // 0-快递, 1-线下交易

    // 🌟 新增：对齐前端文档的支付方式
    private Integer payType;

    // 🌟 修复点：增加 @JsonFormat 确保能正确解析前端传来的 "2024-01-01 12:00:00"
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime meetingTime;

    private String meetingLocation;
}