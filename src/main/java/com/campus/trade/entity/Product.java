package com.campus.trade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrls;       // 多个图片用逗号分隔，也可存JSON，简单起见用字符串
    private Integer status;         // 0-在售 1-已下架 2-已售出
    private Integer viewCount;
    private LocalDateTime createTime;
}