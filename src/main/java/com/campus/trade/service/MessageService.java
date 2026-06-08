package com.campus.trade.service;

import com.campus.trade.dto.request.SendMessageRequest;
import com.campus.trade.dto.response.MessageSessionResponse;
import com.campus.trade.dto.response.MessageUnreadCountResponse;
import com.campus.trade.dto.websocket.WebSocketMessage;
import com.campus.trade.entity.Message;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.Trade;
import com.campus.trade.entity.User;
import com.campus.trade.repository.MessageRepository;
import com.campus.trade.repository.ProductRepository;
import com.campus.trade.repository.TradeRepository;
import com.campus.trade.repository.UserRepository;
import com.campus.trade.websocket.MessageWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private MessageWebSocketHandler messageWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送消息逻辑
     */
    @Transactional
    public void sendMessage(Long senderId, SendMessageRequest req) {
        // 验证：不能给自己发消息
        if (senderId.equals(req.getReceiverId())) {
            throw new com.campus.trade.exception.BusinessException(7001, "不能给自己发消息");
        }
        
        // 验证：接收者不存在
        User receiver = userRepository.findById(req.getReceiverId());
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
        
        // 如果是交易卡片消息，设置交易相关字段
        if (req.getType() == 1) {
            msg.setTradeId(req.getTradeId());
            
            // 自动生成交易快照（不依赖前端传递）
            if (req.getTradeId() != null) {
                msg.setTradeData(generateTradeDataSnapshot(req.getTradeId()));
                // 从快照中获取状态（确保状态与快照一致）
                Trade trade = tradeRepository.findById(req.getTradeId());
                if (trade != null) {
                    msg.setTradeStatus(trade.getStatus());
                } else {
                    msg.setTradeStatus(req.getTradeStatus());
                }
            } else {
                // 没有 tradeId 时，使用前端传递的数据
                msg.setTradeStatus(req.getTradeStatus());
                msg.setTradeData(req.getTradeData());
            }
        }
        
        Long messageId = messageRepository.insert(msg);
        
        // 查询完整的消息记录（包含 createTime）
        Message savedMsg = messageRepository.findById(messageId);
        if (savedMsg == null) {
            log.warn("消息保存后查询失败: messageId={}", messageId);
            return;
        }
        
        // 通过 WebSocket 实时推送
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType("new_message");
        wsMessage.setId(savedMsg.getId());
        wsMessage.setSenderId(savedMsg.getSenderId());
        wsMessage.setReceiverId(savedMsg.getReceiverId());
        wsMessage.setProductId(savedMsg.getProductId());
        wsMessage.setContent(savedMsg.getContent());
        wsMessage.setMessageType(savedMsg.getType());
        wsMessage.setIsRead(savedMsg.getIsRead());
        wsMessage.setCreateTime(savedMsg.getCreateTime());
        wsMessage.setTradeStatus(savedMsg.getTradeStatus());
        wsMessage.setTradeData(savedMsg.getTradeData());
        wsMessage.setTradeId(savedMsg.getTradeId());
        
        boolean sent = messageWebSocketHandler.sendMessage(req.getReceiverId(), wsMessage);
        log.info("消息发送: senderId={}, receiverId={}, webSocketSent={}", senderId, req.getReceiverId(), sent);
    }

    /**
     * 获取聊天历史并自动标记为已读
     */
    public List<Message> getHistory(Long userId, Long targetUserId) {
        // 1. 获取记录（先获取，确保数据可见）
        List<Message> history = messageRepository.findChatHistory(userId, targetUserId);
        
        // 2. 标记对方发给我的消息为已读（在获取之后单独执行，避免事务问题）
        if (!history.isEmpty()) {
            messageRepository.markAsRead(userId, targetUserId);
        }
        
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
            Long targetUserId = msg.getSenderId();
            if (targetUserId.equals(userId)) {
                targetUserId = msg.getReceiverId();
            }
            
            User targetUser = userRepository.findById(targetUserId);
            if (targetUser == null) continue;
            
            // 获取商品信息
            String productName = null;
            if (msg.getProductId() != null) {
                Product product = productRepository.findById(msg.getProductId());
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
            response.setProductId(msg.getProductId());
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

    /**
     * 生成交易数据快照（发送时刻的完整状态）
     * @param tradeId 交易ID
     * @return 交易快照 JSON 字符串
     */
    public String generateTradeDataSnapshot(Long tradeId) {
        try {
            Trade trade = tradeRepository.findById(tradeId);
            if (trade == null) {
                log.warn("交易不存在: tradeId={}", tradeId);
                return null;
            }
            
            Map<String, Object> snapshot = new HashMap<>();
            
            // 交易基本信息
            snapshot.put("tradeId", trade.getId());
            snapshot.put("tradeNo", trade.getTradeNo());
            snapshot.put("status", trade.getStatus());
            snapshot.put("meetingLocation", trade.getMeetingLocation());
            snapshot.put("meetingTime", trade.getMeetingTime() != null ? trade.getMeetingTime().toString() : null);
            snapshot.put("createTime", trade.getCreateTime() != null ? trade.getCreateTime().toString() : null);
            
            // 商品信息
            snapshot.put("productId", trade.getProductId());
            snapshot.put("productName", trade.getProductName());
            snapshot.put("productPrice", trade.getProductPrice());
            snapshot.put("productImage", trade.getProductImage());
            
            // 买家信息
            snapshot.put("buyerId", trade.getBuyerId());
            snapshot.put("buyerName", trade.getBuyerName());
            snapshot.put("buyerAvatar", trade.getBuyerAvatar());
            snapshot.put("buyerCreditScore", trade.getBuyerCreditScore());
            snapshot.put("buyerIsAuth", trade.getBuyerIsAuth() != null && trade.getBuyerIsAuth() == 1);
            snapshot.put("buyerPhone", trade.getBuyerPhone());
            
            // 卖家信息
            snapshot.put("sellerId", trade.getSellerId());
            snapshot.put("sellerName", trade.getSellerName());
            snapshot.put("sellerAvatar", trade.getSellerAvatar());
            snapshot.put("sellerCreditScore", trade.getSellerCreditScore());
            snapshot.put("sellerIsAuth", trade.getSellerIsAuth() != null && trade.getSellerIsAuth() == 1);
            snapshot.put("sellerPhone", trade.getSellerPhone());
            
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.error("生成交易快照失败: tradeId={}", tradeId, e);
            return null;
        }
    }
}