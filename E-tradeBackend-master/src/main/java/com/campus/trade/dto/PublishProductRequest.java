package com.campus.trade.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PublishProductRequest {
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 50)
    private String name;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存至少为1")
    private Integer stock;

    @Size(max = 500)
    private String description;

    private Long categoryId;

    // 图片文件（前端上传多个）
    private List<MultipartFile> images;
}