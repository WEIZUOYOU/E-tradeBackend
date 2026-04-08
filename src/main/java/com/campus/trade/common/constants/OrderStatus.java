package com.campus.trade.common.constants;

import lombok.Getter;

@Getter
public enum OrderStatus {
    WAIT_PAY(0, "待支付"),
    PAID(1, "已支付"),
    DELIVERED(2, "已发货/待交付"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String message;

    OrderStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}