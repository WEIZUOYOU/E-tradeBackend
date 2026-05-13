package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.CreateReportRequest;
import com.campus.trade.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/trade/report")
@Validated
public class ReportController {

    @Autowired
    private ReportService reportService;

    // 提交举报
    @PostMapping
    public Result<Void> submitReport(@Validated @RequestBody CreateReportRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        reportService.submitReport(userId, req.getProductId(), req.getReason());
        return Result.success();
    }

}
