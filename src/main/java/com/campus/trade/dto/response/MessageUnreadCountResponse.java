package com.campus.trade.dto.response;

import lombok.Data;

/**
 * 消息未读统计响应DTO
 */
@Data
public class MessageUnreadCountResponse {
    private Integer totalCount;
    private Integer sessionCount;
}
