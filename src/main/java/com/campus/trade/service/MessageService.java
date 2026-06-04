package com.campus.trade.service;

import com.campus.trade.dto.request.SendMessageRequest;
import com.campus.trade.dto.response.MessageSessionResponse;
import com.campus.trade.dto.response.MessageUnreadCountResponse;
import com.campus.trade.entity.Message;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.repository.MessageRepository;
import com.campus.trade.repository.ProductRepository;
import com.campus.trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 发送消息逻辑
     */
    public void sendMessage(Integer senderId, SendMessageRequest req) {
        // 验证：不能给自己发消息
        if (senderId.equals(req.getReceiverId())) {
            throw new com.campus.trade.exception.BusinessException(7001, "不能给自己发消息");
        }
        
        // 验证：接收者不存在
        User receiver = userRepository.findById(req.getReceiverId().longValue());
        if (receiver == null) {
            throw new com.campus.trade.exception.BusinessException(7004, "接收者不存在");
        }
        
        // 验证：内容为空
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new com.campus.trade.exception.BusinessException(7002, "消息内容为空");
        }
        
        // 验证：长度超限
        if (req.getContent().length() > 500) {
            throw new com.campus.trade.exception.BusinessException(7003, "消息内容长度不能超过500字符");
        }
        
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
    public List<Message> getHistory(Long userId, Long targetUserId) {
        // 1. 获取记录
        List<Message> history = messageRepository.findChatHistory(userId.intValue(), targetUserId.intValue());
        // 2. 标记对方发给我的消息为已读
        messageRepository.markAsRead(userId.intValue(), targetUserId.intValue());
        return history;
    }

    /**
     * 获取用户的所有会话列表
     */
    public List<MessageSessionResponse> getSessions(Long userId) {
        List<Message> sessions = messageRepository.findSessions(userId);
        List<MessageSessionResponse> responses = new ArrayList<>();
        
        for (Message msg : sessions) {
            // 确定对方用户ID
            Long targetUserId = msg.getSenderId().longValue();
            if (targetUserId.equals(userId)) {
                targetUserId = msg.getReceiverId().longValue();
            }
            
            User targetUser = userRepository.findById(targetUserId);
            if (targetUser == null) continue;
            
            // 获取商品信息
            String productName = null;
            if (msg.getProductId() != null) {
                Product product = productRepository.findById(msg.getProductId().longValue());
                if (product != null) {
                    productName = product.getName();
                }
            }
            
            // 统计未读消息数
            int unreadCount = messageRepository.countUnreadBySender(userId, targetUserId);
            
            MessageSessionResponse response = new MessageSessionResponse();
            response.setTargetUserId(targetUserId);
            response.setTargetUserName(targetUser.getUsername());
            response.setTargetUserAvatar(targetUser.getAvatar());
            response.setLastMessage(msg.getContent());
            response.setLastMessageTime(msg.getCreateTime());
            response.setUnreadCount(unreadCount);
            response.setProductId(msg.getProductId() != null ? msg.getProductId().longValue() : null);
            response.setProductName(productName);
            
            responses.add(response);
        }
        
        return responses;
    }

    /**
     * 获取未读消息统计
     */
    public MessageUnreadCountResponse getUnreadCount(Long userId) {
        MessageUnreadCountResponse response = new MessageUnreadCountResponse();
        response.setTotalCount(messageRepository.countUnread(userId));
        response.setSessionCount(messageRepository.countUnreadSessions(userId));
        return response;
    }

    /**
     * 标记单条消息已读
     */
    public void markAsRead(Long messageId, Long userId) {
        messageRepository.markMessageAsRead(messageId, userId);
    }

    /**
     * 标记与某个用户的所有消息已读
     */
    public void markSessionAsRead(Long userId, Long targetUserId) {
        messageRepository.markSessionAsRead(userId, targetUserId);
    }
}