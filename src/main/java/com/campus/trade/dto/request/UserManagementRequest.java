package com.campus.trade.dto.request;

import lombok.Data;

@Data
public class UserManagementRequest {
    private String password; // 重置密码时必填
}
