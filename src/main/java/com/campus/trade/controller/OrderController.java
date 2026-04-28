package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.CreateOrderRequest;
import com.campus.trade.dto.OrderDetailResponse;
import com.campus.trade.entity.Order;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.service.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/trade/order") // 已对齐前端前缀
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 🌟 修复点1：创建订单 RESTful，直接对应 POST /api/v1/trade/order
    @PostMapping
    public Result<Map<String, Object>> createOrder(@Validated @RequestBody CreateOrderRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Order order = orderService.createOrder(userId, req);
        
        // 按照前端文档要求，创建订单返回 orderId 和 orderNo 等基础信息
        Map<String, Object> respData = new HashMap<>();
        respData.put("orderId", order.getId().toString()); // 前端要求 String
        respData.put("orderNo", order.getOrderNo());
        return Result.success(respData);
    }

    // 🌟 修复点2：查询订单详情 RESTful，直接对应 GET /api/v1/trade/order/{id}
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getSession().getAttribute("userId");
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        return Result.success(orderService.getOrderDetail(id, currentUserId));
    }

    // 🌟 修复点3：取消订单，对应 PUT /api/v1/trade/order/{id}/cancel
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpSession session) {
        // 致命错误修复：强转 Integer 改为 Long
        Long userId = (Long) session.getAttribute("userId");
        orderService.cancelOrderByBuyer(id, userId);
        return Result.success();
    }

    // --- 列表查询保持原样即可 ---
    @GetMapping("/buyer/list")
    public Result<List<OrderDetailResponse>> buyerList(@RequestParam(required = false) Integer status, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return Result.success(orderService.getBuyerOrders(userId, status));
    }

    @GetMapping("/seller/list")
    public Result<List<OrderDetailResponse>> sellerList(@RequestParam(required = false) Integer status, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return Result.success(orderService.getSellerOrders(userId, status));
    }

    // --- 状态流转操作 (修改为 PUT 更加符合前端 REST 规范) ---
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        orderService.confirmOrder(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        orderService.deliverOrder(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        orderService.completeOrder(id, userId);
        return Result.success();
    }
}