package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.SendMessageRequest;
import com.campus.trade.dto.response.MessageSessionResponse;
import com.campus.trade.dto.response.MessageUnreadCountResponse;
import com.campus.trade.entity.Message;
import com.campus.trade.service.MessageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送即时消息（支持文字和图片URL）
     */
    @PostMapping("/send")
    public Result<Void> send(@Valid @RequestBody SendMessageRequest req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.sendMessage(userId.intValue(), req);
        return Result.success();
    }

    /**
     * 获取与某个用户的对话列表
     */
    @GetMapping("/history")
    public Result<List<Message>> history(@RequestParam Long targetUserId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<Message> list = messageService.getHistory(userId, targetUserId);
        return Result.success(list);
    }

    /**
     * 获取用户的所有会话列表
     */
    @GetMapping("/sessions")
    public Result<Map<String, Object>> sessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<MessageSessionResponse> sessions = messageService.getSessions(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", sessions.size());
        result.put("sessions", sessions);
        return Result.success(result);
    }

    /**
     * 获取未读消息统计
     */
    @GetMapping("/unread-count")
    public Result<MessageUnreadCountResponse> unreadCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        MessageUnreadCountResponse response = messageService.getUnreadCount(userId);
        return Result.success(response);
    }

    /**
     * 标记单条消息已读
     */
    @PutMapping("/read/{messageId}")
    public Result<Void> markAsRead(@PathVariable Long messageId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.markAsRead(messageId, userId);
        return Result.success();
    }

    /**
     * 批量标记与某个用户的所有消息已读
     */
    @PutMapping("/read-session/{targetUserId}")
    public Result<Void> markSessionAsRead(@PathVariable Long targetUserId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.markSessionAsRead(userId, targetUserId);
        return Result.success();
    }
}