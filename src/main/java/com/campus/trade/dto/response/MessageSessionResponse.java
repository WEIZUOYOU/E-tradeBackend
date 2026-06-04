package com.campus.trade.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息会话响应DTO
 */
@Data
public class MessageSessionResponse {
    private Long targetUserId;
    private String targetUserName;
    private String targetUserAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private Long productId;
    private String productName;
}
