package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.response.CreditDetailResponse;
import com.campus.trade.dto.response.ReviewHistoryResponse;
import com.campus.trade.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    // 获取用户信用档案
    @GetMapping("/{userId}")
    public Result<CreditDetailResponse> getCreditDetail(@PathVariable Long userId) {
        return Result.success(creditService.getCreditDetail(userId));
    }

    // 获取评价历史（分页）
    @GetMapping("/{userId}/reviews")
    public Result<List<ReviewHistoryResponse>> getReviewHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(creditService.getReviewHistory(userId, page, size));
    }

}
