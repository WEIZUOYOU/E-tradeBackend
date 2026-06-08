package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
public class Review {
    private Long id;              // 评价ID
    private Long tradeId;         // 交易ID
    private Long reviewerId;      // 评价者ID
    private Long revieweeId;      // 被评价者ID
    private Integer reviewerType; // 评价者类型：0-买家，1-卖家
    private Integer rating;       // 评分：1-5星
    private String content;       // 评价内容
    private String tags;         // 评价标签（逗号分隔：准时,物品完好,价格合理等）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
