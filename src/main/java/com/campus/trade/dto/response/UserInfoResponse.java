package com.campus.trade.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户公开信息响应DTO
 */
@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String avatar;
    private Integer isAuth;
    private String realName;
    private String studentId;
    private Integer creditScore;
    private Integer status;
    private LocalDateTime createTime;
}
