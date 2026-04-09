package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Integer id;
    private Integer senderId;     // 发送者ID
    private Integer receiverId;   // 接收者ID
    private Integer productId;    // 关联商品ID（可选，方便溯源）
    private String content;       // 消息内容（文字或图片URL）
    private Integer type;          // 0-文本，1-图片
    private Integer isRead;        // 0-未读，1-已读
    private LocalDateTime createTime;
}