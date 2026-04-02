package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.CreateOrderRequest;
import com.campus.trade.dto.OrderDetailResponse;
import com.campus.trade.entity.Order;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/order")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> createOrder(@Validated @RequestBody CreateOrderRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Order order = orderService.createOrder(userId, req);
        return Result.success(order);
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        // 从 session 或拦截器获取当前用户ID
        Long currentUserId = (Long) request.getSession().getAttribute("userId");
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        OrderDetailResponse orderDetail = orderService.getOrderDetail(id, currentUserId);
        return Result.success(orderDetail);
    }
}