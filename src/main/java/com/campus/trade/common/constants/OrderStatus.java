package com.campus.trade.common.constants;

import lombok.Getter;

@Getter
public enum OrderStatus {
    WAIT_SELLER_CONFIRM(0, "待卖家确认"),
    WAIT_TRADING(1, "待交易"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String message;

    OrderStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}