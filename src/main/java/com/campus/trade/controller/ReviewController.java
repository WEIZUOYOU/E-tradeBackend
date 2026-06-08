package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Review;
import com.campus.trade.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/review")
@Validated
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 提交评价
     * POST /api/review
     */
    @PostMapping
    public Result<Void> createReview(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 解析请求参数
        Long tradeId = Long.parseLong(request.get("tradeId").toString());
        Integer rating = Integer.parseInt(request.get("rating").toString());
        String content = (String) request.get("content");
        String tags = (String) request.getOrDefault("tags", "");

        reviewService.createReview(userId, tradeId, rating, content, tags);
        return Result.success();
    }

    /**
     * 获取用户收到的所有评价
     * GET /api/review/received?userId={userId}
     */
    @GetMapping("/received")
    public Result<Map<String, Object>> getReceivedReviews(@RequestParam Long userId) {
        List<Review> reviews = reviewService.getReceivedReviews(userId);
        double avgRating = reviewService.getAverageRating(userId);
        int reviewCount = reviewService.getReceivedReviewCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviews);
        data.put("averageRating", avgRating);
        data.put("reviewCount", reviewCount);

        return Result.success(data);
    }

    /**
     * 获取用户给出的所有评价
     * GET /api/review/given?userId={userId}
     */
    @GetMapping("/given")
    public Result<Map<String, Object>> getGivenReviews(@RequestParam Long userId) {
        List<Review> reviews = reviewService.getGivenReviews(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviews);

        return Result.success(data);
    }

    /**
     * 获取交易的所有评价
     * GET /api/review/trade/{tradeId}
     */
    @GetMapping("/trade/{tradeId}")
    public Result<List<Review>> getTradeReviews(@PathVariable Long tradeId) {
        List<Review> reviews = reviewService.getTradeReviews(tradeId);
        return Result.success(reviews);
    }

    /**
     * 获取用户评价统计
     * GET /api/review/stats?userId={userId}
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getReviewStats(@RequestParam Long userId) {
        Map<String, Object> stats = reviewService.getReviewStats(userId);
        return Result.success(stats);
    }
}
