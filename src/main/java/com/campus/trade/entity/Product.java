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
    private Long categoryId; // 与数据库 BIGINT 一致
    private String name;
    private BigDecimal price; // 当前售价
    private Integer stock;
    private Integer soldCount; // 已售数量
    private String description;

    // 前端期望的字段：List<String>
    private List<String> images;

    // 数据库映射字段（逗号分隔）- 内部使用
    @JsonIgnore
    private String imageUrls;

    private Integer status; // 0-待审核 1-在售 2-已下架 3-已售出 4-审核不通过
    private String reviewReason; // 审核驳回原因
    private Long reviewerId; // 审核人ID
    private LocalDateTime reviewedTime; // 审核时间
    private Integer viewCount;
    private Integer isRecommend; // 是否推荐：0-否，1-是
    private LocalDateTime createTime;
    private String coverImage; // 封面图片

    // 扩展字段（联表查询时填充）
    private String sellerName; // 卖家名称
    private String sellerAvatar; // 卖家头像
    private Integer sellerIsAuth; // 卖家是否实名认证
    private String categoryName; // 分类名称

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