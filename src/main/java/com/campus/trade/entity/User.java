package com.campus.trade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String studentId; // 学号
    private String username;
    private String password; // 实际存储 BCrypt 加密后的密文
    private String phone;
    private String avatar; // 头像 URL（相对路径或绝对路径）
    private Integer creditScore; // 信用分
    private Integer status; // 0-正常 1-冻结
    private Integer verifyStatus; // 0未认证 1审核中 2已认证 3驳回
    private String realName; // 真实姓名
    private LocalDateTime createTime;
}