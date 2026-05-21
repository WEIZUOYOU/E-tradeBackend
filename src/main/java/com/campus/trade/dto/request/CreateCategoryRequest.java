package com.campus.trade.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;
    private String icon;
    private Integer parentId;
    private Integer sortOrder;
    private Integer status;
}
