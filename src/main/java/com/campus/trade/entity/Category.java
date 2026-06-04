package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;        // 分类名称
    private Long parentId;      // 父分类ID，NULL表示一级分类
    private Integer sortOrder;  // 排序顺序
    private Boolean isActive;   // 是否启用：1-启用，0-禁用
    private LocalDateTime createTime;
}