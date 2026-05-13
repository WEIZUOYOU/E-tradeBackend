package com.campus.trade.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "举报理由不能为空")
    @Size(min = 5, max = 255, message = "举报理由长度5-255字符")
    private String reason;
}
