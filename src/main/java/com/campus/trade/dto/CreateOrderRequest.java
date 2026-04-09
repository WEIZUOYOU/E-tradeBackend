package com.campus.trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    @NotNull(message = "购买数量不能为空")
    private Integer quantity;

    @NotNull(message = "地址信息不能为空")
    private Integer addressId; // 即使是线下，通常也关联地址以获取联系电话

    @NotNull(message = "交易方式不能为空")
    private Integer tradeType; // 0-快递, 1-线下交易

    // 线下交易专用字段
    private LocalDateTime meetingTime;    // 约定时间
    private String meetingLocation;       // 约定地点
}