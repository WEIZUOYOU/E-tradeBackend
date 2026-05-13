package com.campus.trade.service;

import com.campus.trade.entity.Product;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.ProductRepository;
import com.campus.trade.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ProductRepository productRepository;

    // 提交举报
    @Transactional
    public void submitReport(Long reporterId, Long productId, String reason) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查举报理由
        if (!StringUtils.hasText(reason) || reason.length() < 5) {
            throw new BusinessException("举报理由必须至少5个字符");
        }

        // 检查是否已举报过该商品（防止重复举报）
        if (reportRepository.exists(reporterId, productId)) {
            throw new BusinessException("您已举报过该商品，请勿重复举报");
        }

        // 检查举报人是否为商品卖家（卖家不能举报自己的商品）
        if (product.getSellerId().equals(reporterId)) {
            throw new BusinessException("不能举报自己的商品");
        }

        reportRepository.insert(reporterId, productId, reason);
    }

    // 后台管理暂未实现，因此此处未实现提醒功能，后续可根据需要添加
}
