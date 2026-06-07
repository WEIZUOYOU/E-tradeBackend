package com.campus.trade.dto.websocket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebSocketMessage {
    private String type; // "new_message" - 新消息, "system_notice" - 系统通知
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long productId;
    private String content;
    private Integer messageType; // 0-文本，1-交易卡片
    private Integer isRead;
    private LocalDateTime createTime;
    private Integer tradeStatus;
    private String tradeData;
    private Long tradeId;
}
