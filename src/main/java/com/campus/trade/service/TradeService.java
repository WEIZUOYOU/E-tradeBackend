package com.campus.trade.service;

import com.campus.trade.entity.Trade;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.User;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.TradeRepository;
import com.campus.trade.repository.ProductRepository;
import com.campus.trade.repository.UserRepository;
import com.campus.trade.dto.request.SendMessageRequest;
import com.campus.trade.websocket.MessageWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageWebSocketHandler messageWebSocketHandler;

    /**
     * 创建交易（买家发起）
     * 业务流程：
     * 1. 校验商品存在且在售
     * 2. 校验买家和卖家均存在
     * 3. 校验非自买
     * 4. 校验双方信用分
     * 5. 扣减库存
     * 6. 创建交易记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Trade createTrade(Long buyerId, Long productId, String meetingLocation, LocalDateTime meetingTime) {
        // 1. 校验商品
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException(3004, "商品不存在");
        }
        // 商品必须是在售状态(1)
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(3006, "该商品当前不可购买");
        }
        // 2. 校验非自买
        if (product.getSellerId() == null || product.getSellerId().equals(buyerId)) {
            throw new BusinessException(3001, "不能购买自己的商品");
        }
        // 3. 校验库存
        if (product.getStock() == null || product.getStock() < 1) {
            throw new BusinessException(3002, "商品库存不足");
        }
        // 4. 校验用户
        User buyer = userRepository.findById(buyerId);
        User seller = userRepository.findById(product.getSellerId());
        if (buyer == null) {
            throw new BusinessException(1001, "买家用户不存在");
        }
        if (seller == null) {
            throw new BusinessException(1001, "卖家用户不存在");
        }
        // 5. 校验双方信用分（双方都需达到最低信用分）
        if (buyer.getCreditScore() != null && buyer.getCreditScore() < 60) {
            throw new BusinessException(3003, "您的信用分过低，无法发起交易");
        }
        if (seller.getCreditScore() != null && seller.getCreditScore() < 60) {
            throw new BusinessException(3003, "卖家信用分过低，无法交易");
        }
        // 6. 扣减库存（带条件更新，防止超卖）
        int rows = productRepository.deductStock(productId, 1);
        if (rows == 0) {
            throw new BusinessException(3002, "商品库存不足");
        }
        // 7. 构建交易对象
        Trade trade = new Trade();
        trade.setTradeNo(generateTradeNo());
        trade.setProductId(productId);
        trade.setBuyerId(buyerId);
        trade.setSellerId(product.getSellerId());
        // 商品信息快照（防止商品信息变更后影响历史交易）
        trade.setProductName(product.getName());
        trade.setProductPrice(product.getPrice());
        trade.setProductImage(product.getCoverImage());
        // 买家信息快照
        trade.setBuyerName(buyer.getUsername());
        trade.setBuyerAvatar(buyer.getAvatar());
        trade.setBuyerCreditScore(buyer.getCreditScore());
        trade.setBuyerIsAuth(buyer.getIsAuth());
        trade.setBuyerPhone(buyer.getPhone());
        // 卖家信息快照
        trade.setSellerName(seller.getUsername());
        trade.setSellerAvatar(seller.getAvatar());
        trade.setSellerCreditScore(seller.getCreditScore());
        trade.setSellerIsAuth(seller.getIsAuth());
        // 交易信息
        trade.setMeetingLocation(meetingLocation);
        trade.setMeetingTime(meetingTime);
        trade.setStatus(0); // 待卖家确认

        tradeRepository.insert(trade);
        log.info("创建交易成功: tradeId={}, tradeNo={}, buyerId={}, sellerId={}",
                trade.getId(), trade.getTradeNo(), buyerId, trade.getSellerId());
        return trade;
    }

    /**
     * 卖家确认交易
     * 业务流程：
     * 1. 校验交易存在
     * 2. 校验操作权限（仅卖家）
     * 3. 校验交易状态（仅待确认状态可确认）
     * 4. 更新卖家电话和状态
     */
    @Transactional(rollbackFor = Exception.class)
    public int confirmTrade(Long tradeId, Long sellerId, String sellerPhone) {
        // 1. 校验交易存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        // 2. 校验操作权限
        if (!trade.getSellerId().equals(sellerId)) {
            throw new BusinessException(3005, "仅卖家可确认交易");
        }
        // 3. 校验交易状态
        if (trade.getStatus() == null || trade.getStatus() != 0) {
            throw new BusinessException(3006, "当前交易状态不允许此操作");
        }
        // 4. 校验电话非空
        if (sellerPhone == null || sellerPhone.trim().isEmpty()) {
            throw new BusinessException(1008, "联系电话不能为空");
        }
        // 5. 更新状态
        int rows = tradeRepository.updateSellerConfirm(tradeId, sellerId, sellerPhone);
        if (rows == 0) {
            throw new BusinessException(3006, "交易确认失败，请刷新后重试");
        }
        
        // 6. 推送交易卡片消息给买家（带幂等性检查）
        // 检查 5 秒内是否已发送过相同的交易卡片消息，避免重复
        boolean alreadySent = messageService.existsTradeCardMessage(
                sellerId, trade.getBuyerId(), tradeId, 1);
        if (!alreadySent) {
            SendMessageRequest msgReq = new SendMessageRequest();
            msgReq.setReceiverId(trade.getBuyerId());
            msgReq.setProductId(trade.getProductId());
            msgReq.setType(1); // 交易卡片消息
            msgReq.setTradeId(tradeId);
            msgReq.setTradeStatus(1); // 待交易
            msgReq.setContent("卖家已确认交易，请查看交易详情");
            messageService.sendMessage(sellerId, msgReq);
            log.info("卖家确认交易成功: tradeId={}, sellerId={}, 已推送消息给买家 buyerId={}", 
                    tradeId, sellerId, trade.getBuyerId());
        } else {
            log.warn("卖家确认交易: tradeId={}, sellerId={}, 检测到 5 秒内已发送相同消息，跳过重复推送", 
                    tradeId, sellerId);
        }
        return 1; // 返回新状态：待交易
    }

    /**
     * 获取交易详情
     * 业务流程：
     * 1. 校验交易存在
     * 2. 校验操作权限（仅买家和卖家可查看）
     */
    public Trade getTradeDetail(Long tradeId, Long currentUserId) {
        // 1. 校验交易存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        // 2. 校验操作权限
        if (!trade.getBuyerId().equals(currentUserId) && !trade.getSellerId().equals(currentUserId)) {
            throw new BusinessException(3005, "无权查看该交易");
        }
        return trade;
    }

    /**
     * 修改交易信息
     * 业务流程：
     * 1. 校验交易存在
     * 2. 校验操作权限（仅买家和卖家可修改）
     * 3. 校验交易状态（仅待交易状态可修改）
     * 4. 记录修改人信息
     * 5. 更新交易时间和地点
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTrade(Long tradeId, Long userId, String meetingLocation, LocalDateTime meetingTime) {
        // 1. 校验交易存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        // 2. 校验操作权限
        boolean isBuyer = trade.getBuyerId().equals(userId);
        boolean isSeller = trade.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(3005, "无权操作该交易");
        }
        // 3. 校验交易状态
        if (trade.getStatus() == null || trade.getStatus() != 1) {
            throw new BusinessException(3006, "仅待交易状态可修改信息");
        }
        // 4. 校验参数
        if (meetingLocation == null || meetingLocation.trim().isEmpty()) {
            throw new BusinessException(1008, "交易地点不能为空");
        }
        if (meetingTime == null) {
            throw new BusinessException(1008, "交易时间不能为空");
        }
        // 5. 更新交易信息（记录最后修改人）
        Long updaterId = userId;
        String updaterRole = isBuyer ? "buyer" : "seller";
        int rows = tradeRepository.updateTradeInfo(tradeId, userId, meetingLocation, meetingTime, updaterId, updaterRole);
        if (rows == 0) {
            throw new BusinessException(3006, "修改失败，请刷新后重试");
        }
        log.info("修改交易信息: tradeId={}, updater={}({}), newTime={}, newLocation={}",
                tradeId, userId, updaterRole, meetingTime, meetingLocation);
    }

    /**
     * 确认对方的修改请求
     * 业务流程：
     * 1. 校验交易存在
     * 2. 校验操作权限
     * 3. 校验交易状态
     * 4. 清除修改标记（确认已读）
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmUpdate(Long tradeId, Long userId) {
        // 1. 校验交易存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        // 2. 校验操作权限
        boolean isBuyer = trade.getBuyerId().equals(userId);
        boolean isSeller = trade.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(3005, "无权操作该交易");
        }
        // 3. 校验交易状态
        if (trade.getStatus() == null || trade.getStatus() != 1) {
            throw new BusinessException(3006, "当前交易状态不允许此操作");
        }
        // 4. 清除修改标记
        int rows = tradeRepository.clearUpdateFlag(tradeId);
        if (rows == 0) {
            throw new BusinessException(3006, "确认失败");
        }
        log.info("确认交易修改: tradeId={}, userId={}", tradeId, userId);
    }

    /**
     * 完成交易（支持状态流转）
     * 
     * 状态转换规则：
     * - 状态 1 (待交易) + SELLER → 状态 2 (卖家已确认)
     * - 状态 1 (待交易) + BUYER → 状态 3 (买家已确认)
     * - 状态 2 (卖家已确认) + BUYER → 状态 4 (已完成)
     * - 状态 3 (买家已确认) + SELLER → 状态 4 (已完成)
     * 
     * @param tradeId 交易ID
     * @param userId 当前用户ID
     * @param operatorType 操作方类型：SELLER 或 BUYER
     * @return 新状态值
     */
    @Transactional(rollbackFor = Exception.class)
    public int completeTrade(Long tradeId, Long userId, String operatorType) {
        // 1. 校验交易存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        
        // 2. 校验操作权限
        boolean isBuyer = trade.getBuyerId().equals(userId);
        boolean isSeller = trade.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(3005, "仅交易双方可操作交易");
        }
        
        // 3. 校验操作方类型与实际身份一致
        if ("SELLER".equals(operatorType) && !isSeller) {
            throw new BusinessException(3005, "您不是该交易的卖家");
        }
        if ("BUYER".equals(operatorType) && !isBuyer) {
            throw new BusinessException(3005, "您不是该交易的买家");
        }
        
        // 4. 获取当前状态并计算新状态
        Integer currentStatus = trade.getStatus();
        if (currentStatus == null) {
            throw new BusinessException(3006, "交易状态异常");
        }
        
        int newStatus;
        switch (currentStatus) {
            case 0: // 待卖家确认
                throw new BusinessException(3006, "请先完成卖家确认，再进行交易确认");
            case 1: // 待交易
                if ("SELLER".equals(operatorType)) {
                    newStatus = 2; // 卖家已确认
                } else {
                    newStatus = 3; // 买家已确认
                }
                break;
            case 2: // 卖家已确认
                if ("BUYER".equals(operatorType)) {
                    newStatus = 4; // 已完成
                } else {
                    throw new BusinessException(3006, "卖家已确认，等待买家确认完成");
                }
                break;
            case 3: // 买家已确认
                if ("SELLER".equals(operatorType)) {
                    newStatus = 4; // 已完成
                } else {
                    throw new BusinessException(3006, "买家已确认，等待卖家确认完成");
                }
                break;
            case 4: // 已完成
                throw new BusinessException(3006, "交易已完成，无需重复确认");
            case 5: // 已取消
                throw new BusinessException(3006, "交易已取消，无法确认");
            default:
                throw new BusinessException(3006, "当前状态不允许此操作");
        }
        
        // 5. 更新状态
        int rows = tradeRepository.updateStatus(tradeId, newStatus);
        if (rows == 0) {
            throw new BusinessException(3006, "操作失败，请刷新后重试");
        }
        
        // 6. 推送交易卡片消息给另一方
        Long receiverId = "SELLER".equals(operatorType) ? trade.getBuyerId() : trade.getSellerId();
        String content;
        if (newStatus == 2) {
            content = "卖家已确认完成交易，请确认完成";
        } else if (newStatus == 3) {
            content = "买家已确认完成交易，请确认完成";
        } else if (newStatus == 4) {
            content = "交易已完成";
        } else {
            content = "交易状态已更新";
        }
        
        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.setReceiverId(receiverId);
        msgReq.setProductId(trade.getProductId());
        msgReq.setType(1); // 交易卡片消息
        msgReq.setTradeId(tradeId);
        msgReq.setTradeStatus(newStatus);
        msgReq.setContent(content);
        messageService.sendMessage(userId, msgReq);
        
        // 7. 如果交易完成，更新商品状态为已售出
        if (newStatus == 4) {
            productRepository.updateStatus(trade.getProductId(), 3); // 3-已售出
            log.info("交易完成，商品已售出: tradeId={}, productId={}", tradeId, trade.getProductId());
        }
        
        log.info("交易状态流转: tradeId={}, {} {} -> {}, 已推送消息给 receiverId={}", tradeId, operatorType, currentStatus, newStatus, receiverId);
        return newStatus;
    }

    /**
     * 取消交易
     * 业务流程：
     * 1. 校验交易存在
     * 2. 校验操作权限
     * 3. 校验交易状态（仅待卖家确认可取消）
     * 4. 恢复库存
     * 5. 更新状态为已取消
     */
    @Transactional(rollbackFor = Exception.class)
    public int cancelTrade(Long tradeId, Long userId) {
        // 1. 校验交易存在
        Trade trade = tradeRepository.findById(tradeId);
        if (trade == null) {
            throw new BusinessException(3004, "交易不存在");
        }
        // 2. 校验操作权限
        boolean isBuyer = trade.getBuyerId().equals(userId);
        boolean isSeller = trade.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(3005, "无权操作该交易");
        }
        // 3. 校验交易状态（仅待卖家确认状态可取消）
        if (trade.getStatus() == null || trade.getStatus() != 0) {
            throw new BusinessException(3006, "仅待确认状态可取消交易");
        }
        // 4. 恢复库存
        int stockRows = productRepository.restoreStock(trade.getProductId(), 1);
        if (stockRows == 0) {
            log.warn("恢复库存失败: tradeId={}, productId={}", tradeId, trade.getProductId());
        }
        // 5. 更新交易状态
        int rows = tradeRepository.cancelTrade(tradeId, userId);
        if (rows == 0) {
            throw new BusinessException(3006, "取消交易失败，请刷新后重试");
        }
        
        // 6. 推送交易卡片消息给另一方
        Long receiverId = isBuyer ? trade.getSellerId() : trade.getBuyerId();
        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.setReceiverId(receiverId);
        msgReq.setProductId(trade.getProductId());
        msgReq.setType(1); // 交易卡片消息
        msgReq.setTradeId(tradeId);
        msgReq.setTradeStatus(5); // 已取消
        msgReq.setContent("交易已取消");
        messageService.sendMessage(userId, msgReq);
        
        log.info("取消交易: tradeId={}, operatorId={}, 已推送消息给 receiverId={}", tradeId, userId, receiverId);
        return 5; // 返回新状态：已取消
    }

    /**
     * 获取我的交易列表（作为买家或卖家）
     */
    public List<Trade> getMyTrades(Long userId, Integer status, int page, int size) {
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        return tradeRepository.findMyTrades(userId, status, page, size);
    }

    /**
     * 获取进行中的交易列表（状态0、1、2、3）
     */
    public List<Trade> getActiveTrades(Long userId, int page, int size) {
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        return tradeRepository.findActiveTrades(userId, page, size);
    }

    /**
     * 获取已完成的交易列表（状态4、5）
     */
    public List<Trade> getCompletedTrades(Long userId, int page, int size) {
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        return tradeRepository.findCompletedTrades(userId, page, size);
    }

    /**
     * 生成交易编号（带UUID确保唯一性）
     * 格式：yyyyMMddHHmmss + 4位随机数
     */
    private String generateTradeNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 使用UUID后4位作为随机数，避免重启后重复
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return timestamp + random;
    }
}