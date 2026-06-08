package com.campus.trade.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateTradeReviewRequest {
    @NotNull(message = "交易ID不能为空")
    private Long tradeId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    private String content; // 评价内容可选

    private String tags; // 标签（逗号分隔）
}