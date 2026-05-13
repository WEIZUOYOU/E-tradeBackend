package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Notification;
import com.campus.trade.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 通知列表
    @GetMapping("/list")
    public Result<List<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(notificationService.listNotifications(userId, page, size));
    }

    // 未读数量
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        int count = notificationService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    // 标记已读
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }

    // 全部已读
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }
}
