package com.campus.trade.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 批量查询用户信息请求DTO
 */
@Data
public class BatchUserInfoRequest {
    private List<Long> userIds;
}
