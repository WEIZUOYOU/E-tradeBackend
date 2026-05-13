package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;       // 接收通知的用户ID
    private Integer type;      // 0-系统通知, 1-举报处理结果, 2-审核结果
    private String title;      // 通知标题
    private String content;    // 通知内容
    private Long relatedId;    // 关联ID（商品ID/举报ID等）
    private Integer isRead;    // 0-未读, 1-已读
    private LocalDateTime createTime;
}
