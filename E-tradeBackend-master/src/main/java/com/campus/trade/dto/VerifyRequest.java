package com.campus.trade.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class VerifyRequest {

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    private String realName;
}