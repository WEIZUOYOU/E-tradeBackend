package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.SendMessageRequest;
import com.campus.trade.entity.Message;
import com.campus.trade.service.MessageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        Integer userId = (Integer) session.getAttribute("userId");
        messageService.sendMessage(userId, req);
        return Result.success();
    }

    /**
     * 获取与某个用户的对话列表
     */
    @GetMapping("/history")
    public Result<List<Message>> history(@RequestParam Integer targetUserId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<Message> list = messageService.getHistory(userId, targetUserId);
        return Result.success(list);
    }
}