package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Integer id;
    private String name;        // 分类名称
    private String description; // 分类描述
    private String icon;        // 分类图标URL
    private Integer parentId;   // 父分类ID，0表示一级分类
    private Integer sortOrder;  // 排序权重
    private Integer status;     // 状态：0-禁用，1-正常
    private LocalDateTime createTime;
}