package com.campus.trade.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    // --- 1. 通用响应 ---
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "登录已失效，请重新登录"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    INTERNAL_ERROR(500, "服务器开小差了，请稍后再试"),

    // --- 2. 用户相关 (1000-1999) ---
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "学号或密码错误"),
    USER_ALREADY_AUTH(1003, "您已完成实名认证，无需重复申请"),
    USER_NOT_AUTH(1004, "请先完成实名认证再进行交易"),
    STUDENT_ID_NOT_FOUND(1005, "校园库中未查找到该学号信息"),

    // --- 3. 商品/库存相关 (2000-2999) ---
    PRODUCT_OFFLINE(2001, "商品已下架或已售罄"),
    STOCK_INSUFFICIENT(2002, "商品库存不足"),
    FILE_UPLOAD_ERROR(2003, "图片上传失败"),

    // --- 4. 订单/交易相关 (3000-3999) ---
    ORDER_STATUS_ERROR(3001, "订单状态异常，无法操作"),
    SELF_BUY_FORBIDDEN(3002, "无法购买自己发布的商品"),
    CREDIT_TOO_LOW(3003, "信用分过低，无法发起交易"),

    // --- 5. 业务通用错误 ---
    BUSINESS_ERROR(5000, "业务处理逻辑错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}