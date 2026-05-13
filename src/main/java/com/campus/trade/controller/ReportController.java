package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.CreateReportRequest;
import com.campus.trade.dto.response.ReportResponse;
import com.campus.trade.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;

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

    // 举报列表
    @GetMapping("/list")
    public Result<List<ReportResponse>> listReports(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(reportService.listReports(status, page, size));
    }

    // 处理举报（下架商品 + 封号 + 通知）
    @PutMapping("/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        reportService.handleReport(id, userId);
        return Result.success(null);
    }

    // 驳回举报
    @PutMapping("/{id}/dismiss")
    public Result<Void> dismissReport(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        reportService.dismissReport(id, userId);
        return Result.success(null);
    }

}
