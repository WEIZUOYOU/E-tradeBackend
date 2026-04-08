package com.campus.trade.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor // Jackson 反序列化需要无参构造函数
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private long timestamp; // 新增：响应时间戳

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回 - 携带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回 - 不携带数据（用于删除、更新等）
     */
    public static <T> Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 错误返回 - 使用枚举定义
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 错误返回 - 使用枚举定义 + 自定义提示语
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 错误返回 - 完全自定义
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}