package com.campus.trade.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateFavoriteRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
