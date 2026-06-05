package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;              // 消息ID（改为Long）
    private Long senderId;        // 发送者ID（改为Long）
    private Long receiverId;      // 接收者ID（改为Long）
    private Long productId;       // 关联商品ID（改为Long）
    private String content;       // 消息内容（文字或图片URL）
    private Integer type;         // 0-文本，1-交易卡片
    private Integer isRead;       // 0-未读，1-已读
    private LocalDateTime createTime;
    
    // 交易相关字段（用于交易卡片消息）
    private Integer tradeStatus;   // 交易状态
    private String tradeData;      // 交易数据JSON
    private Long tradeId;          // 交易ID
}