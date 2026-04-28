package com.campus.trade.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor // Jackson 反序列化需要无参构造函数
public class Result<T> implements Serializable {
    private int code;
    
    // 🌟 修复点：将 message 改为 msg，完美对齐前端文档的 {"code":0, "msg":"成功"}
    private String msg; 
    private T data;
    private long timestamp;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        // 如果你的 ResultCode.SUCCESS.getMessage() 存在，继续用，它会自动赋值给 msg
        return new Result<>(200, "成功", data); 
    }

    public static <T> Result<Void> success() {
        return new Result<>(200, "成功", null);
    }

    // 错误返回 - 使用枚举定义 (假设你的枚举叫 ResultCode)
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}