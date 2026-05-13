package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.CreateFavoriteRequest;
import com.campus.trade.dto.response.FavoriteResponse;
import com.campus.trade.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trade/favorite")
@Validated
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // 添加收藏
    @PostMapping
    public Result<Void> addFavorite(@Validated @RequestBody CreateFavoriteRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        favoriteService.addFavorite(userId, req.getProductId());
        return Result.success();
    }

    // 取消收藏
    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(@PathVariable Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        favoriteService.removeFavorite(userId, productId);
        return Result.success();
    }

    // 检查收藏状态
    @GetMapping("/{productId}/status")
    public Result<Map<String, Object>> getFavoriteStatus(@PathVariable Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean isFavorited = favoriteService.isFavorited(userId, productId);
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("isFavorited", isFavorited);
        return Result.success(data);
    }

    // 获取收藏列表
    @GetMapping
    public Result<List<FavoriteResponse>> getFavoriteList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(favoriteService.getFavoriteList(userId, page, size));
    }
}
