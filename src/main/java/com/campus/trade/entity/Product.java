package com.campus.trade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
public class Product {
    private Long id;
    private Long sellerId;
    private Integer categoryId; // 改为 Integer，与数据库 INT 一致
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;

    // 前端期望的字段：List<String>
    private List<String> images;

    // 数据库映射字段（逗号分隔）- 内部使用
    @JsonIgnore
    private String imageUrls;

    private Integer status; // 0-待审核 1-在售 2-已下架 3-已售出
    private Integer viewCount;
    private LocalDateTime createTime;
    private String coverImage;

    /**
     * 自动将 imageUrls（String）转换为 images（List）
     */
    public List<String> getImages() {
        if (images != null) {
            return images;
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return Arrays.asList(imageUrls.split(","));
        }
        return List.of();
    }

    /**
     * 自动将 images（List）转换为 imageUrls（String）
     */
    public void setImages(List<String> images) {
        this.images = images;
        if (images != null && !images.isEmpty()) {
            this.imageUrls = String.join(",", images);
        }
    }

    /**
     * 设置数据库字段时自动解析为 List
     */
    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.images = Arrays.asList(imageUrls.split(","));
        }
    }
}