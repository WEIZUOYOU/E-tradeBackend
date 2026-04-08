package com.campus.trade.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Integer id;
    private String name;
    private String icon;
    private Integer parentId;
}