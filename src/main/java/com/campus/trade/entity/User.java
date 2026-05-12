package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id; // 改为 Long，与数据库 BIGINT 一致
    private String studentId; // 学号
    private String username; // 用户名
    private String password; // 加密密文
    private String phone;
    private String avatar; // 头像 URL
    private Integer status; // 状态：0-禁用，1-正常
    private Integer isAuth; // 是否实名认证：0-否，1-是 (新增)
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private Integer creditScore; // 信用分
    private String realName; // 真实姓名
}