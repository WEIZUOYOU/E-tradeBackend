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
    USER_ALREADY_FROZEN(1006, "账号已被冻结"),
    USER_NOT_FROZEN(1007, "账号未被冻结，无需解冻"),
    PASSWORD_REQUIRED(1008, "新密码不能为空"),
    USER_NOT_FOUND(1009, "用户不存在"),
    USER_FROZEN(1010, "用户已冻结"),

    // --- 3. 商品/库存相关 (2000-2999) ---
    PRODUCT_OFFLINE(2001, "商品已下架或已售罄"),
    STOCK_INSUFFICIENT(2002, "商品库存不足"),
    FILE_UPLOAD_ERROR(2003, "图片上传失败"),
    PRODUCT_NOT_PENDING(2004, "商品非待审核状态"),
    REVIEW_REASON_REQUIRED(2005, "驳回时必须填写原因"),
    INVALID_CATEGORY(2006, "无效的商品分类"),
    PRODUCT_NOT_FOUND(2007, "商品不存在"),
    NO_PERMISSION(2008, "无权操作"),
    FILE_FORMAT_ERROR(2009, "格式不支持"),
    FILE_TOO_LARGE(2010, "单图大小不能超过5MB"),
    FILE_COUNT_EXCEEDED(2011, "图片数量不能超过9张"),

    // --- 4. 订单/交易相关 (3000-3999) ---
    CANNOT_BUY_OWN_PRODUCT(3001, "不能购买自己的商品"),
    PRODUCT_STOCK_INSUFFICIENT(3002, "商品库存不足"),
    CREDIT_TOO_LOW_FOR_TRADE(3003, "信用分过低，无法交易"),
    TRADE_NOT_FOUND(3004, "交易不存在"),
    NO_TRADE_PERMISSION(3005, "无操作权限"),
    TRADE_STATUS_NOT_ALLOWED(3006, "交易状态不允许此操作"),

    // --- 5. 举报相关 (4000-4999) ---
    REPORT_ALREADY_HANDLED(4001, "该举报已处理"),
    REPORT_NOT_FOUND(4002, "举报记录不存在"),

    // --- 6. 收藏相关 (5000-5999) ---
    FAVORITE_EXISTS(5001, "商品已收藏，请勿重复操作"),
    FAVORITE_NOT_FOUND(5002, "收藏记录不存在"),

    // --- 7. 评价相关 (6000-6999) ---
    ORDER_NOT_COMPLETED(6001, "订单未完成，无法评价"),
    REVIEW_ALREADY_EXISTS(6002, "该订单已评价"),
    SELF_REVIEW_FORBIDDEN(6003, "不能评价自己"),

    // --- 8. 消息相关 (7000-7999) ---
    MESSAGE_SELF_FORBIDDEN(7001, "不能给自己发消息"),
    MESSAGE_EMPTY(7002, "消息内容为空"),
    MESSAGE_TOO_LONG(7003, "消息内容长度不能超过500字符"),
    RECEIVER_NOT_FOUND(7004, "接收者不存在"),

    // --- 9. 业务通用错误 ---
    BUSINESS_ERROR(9000, "业务处理逻辑错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}