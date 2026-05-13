package com.campus.trade.service;

import com.campus.trade.entity.Notification;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.NotificationRepository;
import com.campus.trade.utils.WeChatNotifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WeChatNotifyUtil weChatNotifyUtil;

    /**
     * 创建通知并尝试微信推送
     */
    public void createAndSend(Long userId, Integer type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notificationRepository.insert(notification);

        // 尝试微信模板消息推送（用户未绑定则跳过）
        weChatNotifyUtil.sendTemplateMessage(null, title, content);
    }

    /**
     * 获取用户通知列表
     */
    public List<Notification> listNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserId(userId, page, size);
    }

    /**
     * 获取未读通知数
     */
    public int getUnreadCount(Long userId) {
        return notificationRepository.getUnreadCount(userId);
    }

    /**
     * 标记单条已读
     */
    public void markAsRead(Long notificationId, Long userId) {
        int rows = notificationRepository.markAsRead(notificationId, userId);
        if (rows == 0) {
            throw new BusinessException("通知不存在或无权操作");
        }
    }

    /**
     * 全部标记已读
     */
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
