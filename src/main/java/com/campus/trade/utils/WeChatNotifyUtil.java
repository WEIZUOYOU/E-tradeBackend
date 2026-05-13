package com.campus.trade.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微信模板消息推送工具
 * 当前为骨架实现，配置微信公众号/小程序 AppId 和 AppSecret 后即可启用真实推送
 */
@Slf4j
@Component
public class WeChatNotifyUtil {

    // TODO: 从配置文件注入
    // @Value("${wechat.app-id}")
    // private String appId;
    // @Value("${wechat.app-secret}")
    // private String appSecret;

    /**
     * 发送微信模板消息
     * @param openId  接收者 openId
     * @param title   消息标题
     * @param content 消息内容
     */
    public void sendTemplateMessage(String openId, String title, String content) {
        if (openId == null || openId.isEmpty()) {
            log.info("用户未绑定微信openId，跳过微信推送");
            return;
        }
        // TODO: 接入微信模板消息 API
        // 1. 获取 access_token
        // 2. 调用 POST https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=TOKEN
        // 3. 传入模板ID、openId、模板数据
        log.info("[微信模板消息] 接收者: {}, 标题: {}, 内容: {}", openId, title, content);
    }
}
