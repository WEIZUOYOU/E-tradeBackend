package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.CreateReviewRequest;
import com.campus.trade.dto.response.ReviewResponse;
import com.campus.trade.service.OrderReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trade/review")
@Validated
public class OrderReviewController {

    @Autowired
    private OrderReviewService orderReviewService;

    // 提交评价
    @PostMapping
    public Result<Void> createReview(@Validated @RequestBody CreateReviewRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        orderReviewService.createReview(userId, req);
        return Result.success();
    }

    // 获取订单评价
    @GetMapping("/{orderNo}")
    public Result<List<ReviewResponse>> getOrderReviews(@PathVariable String orderNo) {
        return Result.success(orderReviewService.getOrderReviews(orderNo));
    }
}
