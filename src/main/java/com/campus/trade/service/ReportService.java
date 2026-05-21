package com.campus.trade.service;

import com.campus.trade.dto.response.ReportResponse;
import com.campus.trade.entity.Product;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.ProductRepository;
import com.campus.trade.repository.ReportRepository;
import com.campus.trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.adminIds:1}")
    private String adminIds;

    // 提交举报
    @Transactional
    public void submitReport(Long reporterId, Long productId, String reason) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (!StringUtils.hasText(reason) || reason.length() < 5) {
            throw new BusinessException("举报理由必须至少5个字符");
        }

        if (reportRepository.exists(reporterId, productId)) {
            throw new BusinessException("您已举报过该商品，请勿重复举报");
        }

        if (product.getSellerId().equals(reporterId)) {
            throw new BusinessException("不能举报自己的商品");
        }

        reportRepository.insert(reporterId, productId, reason);

        // 通知管理员
        String title = "新举报待处理";
        String content = "用户ID " + reporterId + " 对商品ID " + productId + " 提交了举报，请尽快处理。";
        if (adminIds != null && !adminIds.isBlank()) {
            for (String s : adminIds.split(",")) {
                try {
                    Long adminId = Long.parseLong(s.trim());
                    notificationService.createAndSend(
                            adminId,
                            0, // 类型：系统通知（或按你需要改为其他类型）
                            title,
                            content,
                            productId);
                } catch (NumberFormatException ignore) {
                }
            }
        }
    }

    // 举报列表
    public List<ReportResponse> listReports(Integer status, int page, int size) {
        if (status != null) {
            return reportRepository.findByStatus(status, page, size);
        }
        return reportRepository.findAll(page, size);
    }

    // 处理举报（下架商品 + 冻结卖家）
    @Transactional
    public void handleReport(Long reportId, Long handlerId) {
        ReportResponse report = reportRepository.findById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (report.getStatus() != 0) {
            throw new BusinessException("该举报已处理，无需重复操作");
        }

        // 1. 下架商品
        Product product = productRepository.findById(report.getProductId());
        if (product != null) {
            productRepository.updateStatus(report.getProductId(), 2); // status=2 已下架
        }

        // 2. 冻结卖家账号
        if (product != null && product.getSellerId() != null) {
            userRepository.updateStatus(product.getSellerId(), 0); // status=0 禁用
        }

        // 3. 更新举报状态
        reportRepository.handleReport(reportId, 1, handlerId, "已处理：商品下架，卖家封号");

        // 4. 通知卖家
        if (product != null) {
            notificationService.createAndSend(
                    product.getSellerId(), 1,
                    "商品被举报处理",
                    "您的商品「" + product.getName() + "」因被举报已下架，账号已被冻结。如有疑问请联系管理员。",
                    report.getProductId());
        }

        // 5. 通知举报人
        notificationService.createAndSend(
                report.getReporterId(), 1,
                "举报已处理",
                "您举报的商品已被处理，感谢您的监督。",
                report.getProductId());
    }

    // 驳回举报
    @Transactional
    public void dismissReport(Long reportId, Long handlerId) {
        ReportResponse report = reportRepository.findById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (report.getStatus() != 0) {
            throw new BusinessException("该举报已处理，无需重复操作");
        }

        reportRepository.handleReport(reportId, 2, handlerId, "已驳回：举报不成立");

        // 通知举报人
        notificationService.createAndSend(
                report.getReporterId(), 1,
                "举报处理结果",
                "您举报的商品经审核不构成违规，举报已被驳回。",
                report.getProductId());
    }
}
