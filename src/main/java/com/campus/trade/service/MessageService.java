package com.campus.trade.service;

import com.campus.trade.dto.SendMessageRequest;
import com.campus.trade.entity.Message;
import com.campus.trade.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 发送消息逻辑
     */
    public void sendMessage(Integer senderId, SendMessageRequest req) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(req.getReceiverId());
        msg.setProductId(req.getProductId());
        msg.setContent(req.getContent());
        msg.setType(req.getType());
        messageRepository.insert(msg);
    }

    /**
     * 获取聊天历史并自动标记为已读
     */
    @Transactional
    public List<Message> getHistory(Integer userId, Integer targetUserId) {
        // 1. 获取记录
        List<Message> history = messageRepository.findChatHistory(userId, targetUserId);
        // 2. 标记对方发给我的消息为已读
        messageRepository.markAsRead(userId, targetUserId);
        return history;
    }
}