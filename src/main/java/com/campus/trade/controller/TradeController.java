package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Trade;
import com.campus.trade.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    /**
     * 创建交易
     * POST /api/trade/create
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createTrade(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 参数校验
        Object productIdObj = request.get("productId");
        if (productIdObj == null) {
            return Result.error(1001, "商品ID不能为空");
        }
        Long productId = ((Number) productIdObj).longValue();

        String meetingLocation = (String) request.get("meetingLocation");
        if (meetingLocation == null || meetingLocation.trim().isEmpty()) {
            return Result.error(1001, "交易地点不能为空");
        }

        Object meetingTimeObj = request.get("meetingTime");
        if (meetingTimeObj == null) {
            return Result.error(1001, "交易时间不能为空");
        }
        
        // 处理字符串格式的时间，支持多种格式
        LocalDateTime meetingTime;
        if (meetingTimeObj instanceof LocalDateTime) {
            meetingTime = (LocalDateTime) meetingTimeObj;
        } else {
            String meetingTimeStr = String.valueOf(meetingTimeObj);
            try {
                // 尝试 ISO_LOCAL_DATE_TIME 格式（如 2026-06-05T10:30:00）
                meetingTime = LocalDateTime.parse(meetingTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                try {
                    // 尝试带时区的格式（如 2026-06-05T10:30:00+08:00）
                    meetingTime = LocalDateTime.parse(meetingTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (Exception e2) {
                    return Result.error(1001, "交易时间格式错误，请使用 ISO 8601 格式（如 2026-06-05T10:30:00）");
                }
            }
        }

        Trade trade = tradeService.createTrade(userId, productId, meetingLocation, meetingTime);

        Map<String, Object> respData = new HashMap<>();
        respData.put("tradeId", trade.getId().toString());
        respData.put("tradeNo", trade.getTradeNo());
        respData.put("status", trade.getStatus());
        return Result.success(respData);
    }

    /**
     * 卖家确认交易
     * POST /api/trade/confirm
     */
    @PostMapping("/confirm")
    public Result<Map<String, Object>> confirmTrade(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Object tradeIdObj = request.get("tradeId");
        if (tradeIdObj == null) {
            return Result.error(1001, "交易ID不能为空");
        }
        Long tradeId = ((Number) tradeIdObj).longValue();

        String sellerPhone = (String) request.get("sellerPhone");
        if (sellerPhone == null || sellerPhone.trim().isEmpty()) {
            return Result.error(1001, "卖家联系电话不能为空");
        }

        int newStatus = tradeService.confirmTrade(tradeId, userId, sellerPhone);
        
        // 返回更新后的状态
        Map<String, Object> data = new HashMap<>();
        data.put("tradeStatus", newStatus);
        return Result.success(data);
    }

    /**
     * 获取交易详情
     * GET /api/trade/detail/{tradeId}
     */
    @GetMapping("/detail/{tradeId}")
    public Result<Map<String, Object>> getTradeDetail(@PathVariable Long tradeId, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        Trade trade = tradeService.getTradeDetail(tradeId, userId);
        
        // 转换为统一格式，确保 buyerIsAuth/sellerIsAuth 为 boolean 类型
        Map<String, Object> result = new HashMap<>();
        result.put("id", trade.getId());
        result.put("tradeNo", trade.getTradeNo());
        result.put("productId", trade.getProductId());
        result.put("buyerId", trade.getBuyerId());
        result.put("sellerId", trade.getSellerId());
        result.put("productName", trade.getProductName());
        result.put("productPrice", trade.getProductPrice());
        result.put("productImage", trade.getProductImage());
        
        // 买家信息
        result.put("buyerName", trade.getBuyerName());
        result.put("buyerAvatar", trade.getBuyerAvatar());
        result.put("buyerCreditScore", trade.getBuyerCreditScore());
        result.put("buyerIsAuth", trade.getBuyerIsAuth() != null && trade.getBuyerIsAuth() == 1); // boolean
        result.put("buyerPhone", trade.getBuyerPhone());
        
        // 卖家信息
        result.put("sellerName", trade.getSellerName());
        result.put("sellerAvatar", trade.getSellerAvatar());
        result.put("sellerCreditScore", trade.getSellerCreditScore());
        result.put("sellerIsAuth", trade.getSellerIsAuth() != null && trade.getSellerIsAuth() == 1); // boolean
        result.put("sellerPhone", trade.getSellerPhone());
        
        result.put("meetingLocation", trade.getMeetingLocation());
        result.put("meetingTime", trade.getMeetingTime());
        result.put("status", trade.getStatus());
        result.put("createTime", trade.getCreateTime());
        result.put("updateTime", trade.getUpdateTime());
        
        return Result.success(result);
    }

    /**
     * 修改交易信息
     * POST /api/trade/update
     */
    @PostMapping("/update")
    public Result<Void> updateTrade(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Object tradeIdObj = request.get("tradeId");
        if (tradeIdObj == null) {
            return Result.error(1001, "交易ID不能为空");
        }
        Long tradeId = ((Number) tradeIdObj).longValue();

        String meetingLocation = (String) request.get("meetingLocation");
        if (meetingLocation == null || meetingLocation.trim().isEmpty()) {
            return Result.error(1001, "交易地点不能为空");
        }

        Object meetingTimeObj = request.get("meetingTime");
        if (meetingTimeObj == null) {
            return Result.error(1001, "交易时间不能为空");
        }
        
        // 处理字符串格式的时间，支持多种格式
        LocalDateTime meetingTime;
        if (meetingTimeObj instanceof LocalDateTime) {
            meetingTime = (LocalDateTime) meetingTimeObj;
        } else {
            String meetingTimeStr = String.valueOf(meetingTimeObj);
            try {
                // 尝试 ISO_LOCAL_DATE_TIME 格式（如 2026-06-05T10:30:00）
                meetingTime = LocalDateTime.parse(meetingTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                try {
                    // 尝试带时区的格式（如 2026-06-05T10:30:00+08:00）
                    meetingTime = LocalDateTime.parse(meetingTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (Exception e2) {
                    return Result.error(1001, "交易时间格式错误，请使用 ISO 8601 格式（如 2026-06-05T10:30:00）");
                }
            }
        }

        tradeService.updateTrade(tradeId, userId, meetingLocation, meetingTime);
        return Result.success();
    }

    /**
     * 确认对方的修改请求
     * POST /api/trade/confirmUpdate
     */
    @PostMapping("/confirmUpdate")
    public Result<Void> confirmUpdate(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Object tradeIdObj = request.get("tradeId");
        if (tradeIdObj == null) {
            return Result.error(1001, "交易ID不能为空");
        }
        Long tradeId = ((Number) tradeIdObj).longValue();

        tradeService.confirmUpdate(tradeId, userId);
        return Result.success();
    }

    /**
     * 完成交易（支持状态流转）
     * POST /api/trade/complete
     * 
     * 状态转换规则：
     * - 状态 1 (待交易) + SELLER → 状态 2 (卖家已确认)
     * - 状态 1 (待交易) + BUYER → 状态 3 (买家已确认)
     * - 状态 2 (卖家已确认) + BUYER → 状态 5 (已完成)
     * - 状态 3 (买家已确认) + SELLER → 状态 5 (已完成)
     */
    @PostMapping("/complete")
    public Result<Map<String, Object>> completeTrade(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Object tradeIdObj = request.get("tradeId");
        if (tradeIdObj == null) {
            return Result.error(1001, "交易ID不能为空");
        }
        Long tradeId = ((Number) tradeIdObj).longValue();

        String operatorType = (String) request.get("operatorType");
        if (operatorType == null || operatorType.trim().isEmpty()) {
            return Result.error(1001, "操作方类型不能为空");
        }

        int newStatus = tradeService.completeTrade(tradeId, userId, operatorType);
        
        Map<String, Object> data = new HashMap<>();
        data.put("tradeStatus", newStatus);
        return Result.success(data);
    }

    /**
     * 取消交易（仅待卖家确认状态可取消）
     * POST /api/trade/cancel
     */
    @PostMapping("/cancel")
    public Result<Map<String, Object>> cancelTrade(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Object tradeIdObj = request.get("tradeId");
        if (tradeIdObj == null) {
            return Result.error(1001, "交易ID不能为空");
        }
        Long tradeId = ((Number) tradeIdObj).longValue();

        int newStatus = tradeService.cancelTrade(tradeId, userId);
        
        // 返回更新后的状态
        Map<String, Object> data = new HashMap<>();
        data.put("tradeStatus", newStatus);
        return Result.success(data);
    }

    /**
     * 获取我的交易列表
     * GET /api/trade/my/list?status=1&page=1&size=10
     */
    @GetMapping("/my/list")
    public Result<Map<String, Object>> myTradeList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 参数边界处理
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        List<Trade> trades = tradeService.getMyTrades(userId, status, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("list", trades);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 获取进行中的订单
     * GET /api/trade/active?page=1&size=10
     * 返回状态为0、1、2、3的订单
     */
    @GetMapping("/active")
    public Result<Map<String, Object>> getActiveTrades(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 参数边界处理
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        List<Trade> trades = tradeService.getActiveTrades(userId, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("list", trades);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 获取已完成的订单
     * GET /api/trade/completed?page=1&size=10
     * 返回状态为4（已完成）、5（已取消）的订单
     */
    @GetMapping("/completed")
    public Result<Map<String, Object>> getCompletedTrades(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 参数边界处理
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        List<Trade> trades = tradeService.getCompletedTrades(userId, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("list", trades);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 从Session中获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }
        // 兼容 Long 和 Integer 类型
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }
}