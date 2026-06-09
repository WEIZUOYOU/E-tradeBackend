package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.CreateTradeReviewRequest;
import com.campus.trade.dto.response.ReviewListResponse;
import com.campus.trade.entity.Review;
import com.campus.trade.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Void> createReview(@Valid @RequestBody CreateTradeReviewRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        reviewService.createReview(userId, request.getTradeId(), request.getRating(), 
                                   request.getContent(), request.getTags());
        return Result.success();
    }

    /**
     * 获取用户收到的所有评价
     * GET /api/review/received
     */
    @GetMapping("/received")
    public Result<ReviewListResponse> getReceivedReviews(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        List<Review> reviews = reviewService.getReceivedReviews(userId);
        double avgRating = reviewService.getAverageRating(userId);
        int reviewCount = reviewService.getReceivedReviewCount(userId);

        ReviewListResponse response = new ReviewListResponse();
        response.setReviews(reviews);
        response.setAverageRating(avgRating);
        response.setReviewCount(reviewCount);

        return Result.success(response);
    }

    /**
     * 获取用户给出的所有评价
     * GET /api/review/given
     */
    @GetMapping("/given")
    public Result<ReviewListResponse> getGivenReviews(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        List<Review> reviews = reviewService.getGivenReviews(userId);
        double avgRating = reviewService.getGivenAverageRating(userId);
        int reviewCount = reviewService.getGivenReviewCount(userId);

        ReviewListResponse response = new ReviewListResponse();
        response.setReviews(reviews);
        response.setAverageRating(avgRating);
        response.setReviewCount(reviewCount);

        return Result.success(response);
    }

    /**
     * 获取单条评价详情
     * GET /api/review/{id}
     */
    @GetMapping("/{id}")
    public Result<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        return Result.success(review);
    }

    /**
     * 删除评价
     * DELETE /api/review/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        reviewService.deleteReview(id, userId);
        return Result.success();
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
    public Result<java.util.Map<String, Object>> getReviewStats(@RequestParam Long userId) {
        java.util.Map<String, Object> stats = reviewService.getReviewStats(userId);
        return Result.success(stats);
    }
}
