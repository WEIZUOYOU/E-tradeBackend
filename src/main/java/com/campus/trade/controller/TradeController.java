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
    public Result<Void> confirmTrade(@RequestBody Map<String, Object> request, HttpSession session) {
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

        tradeService.confirmTrade(tradeId, userId, sellerPhone);
        return Result.success();
    }

    /**
     * 获取交易详情
     * GET /api/trade/detail/{tradeId}
     */
    @GetMapping("/detail/{tradeId}")
    public Result<Trade> getTradeDetail(@PathVariable Long tradeId, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(tradeService.getTradeDetail(tradeId, userId));
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
    public Result<Void> cancelTrade(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Object tradeIdObj = request.get("tradeId");
        if (tradeIdObj == null) {
            return Result.error(1001, "交易ID不能为空");
        }
        Long tradeId = ((Number) tradeIdObj).longValue();

        tradeService.cancelTrade(tradeId, userId);
        return Result.success();
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