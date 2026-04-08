package com.campus.trade.common.constants;

import lombok.Getter;

@Getter
public enum TradeType {
    ONLINE(0, "快递邮寄"),
    OFFLINE(1, "线下交易");

    private final int code;
    private final String message;

    TradeType(int code, String message) {
        this.code = code;
        this.message = message;
    }
}