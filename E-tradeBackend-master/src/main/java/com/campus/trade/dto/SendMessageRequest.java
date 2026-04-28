package com.campus.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotNull(message = "接收者不能为空")
    private Integer receiverId;

    private Integer productId;    // 可以为空

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer type;         // 0:文字, 1:图片URL
}