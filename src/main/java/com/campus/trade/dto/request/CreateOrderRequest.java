package com.campus.trade.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @NotNull(message = "收货地址不能为空")
    private Integer addressId;

    @NotNull(message = "交易方式不能为空")
    private Integer tradeType; // 0-快递, 1-线下

    private LocalDateTime meetingTime; // 线下模式必填
    private String meetingLocation;    // 线下模式必填
}