package com.campus.trade.dto.response;

import com.campus.trade.entity.Product;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteResponse {
    private Long id;
    private Long productId;
    private Product product; // 商品详情
    private LocalDateTime createTime;
}
