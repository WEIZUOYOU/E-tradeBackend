package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.ReviewProductRequest;
import com.campus.trade.dto.request.PublishProductRequest;
import com.campus.trade.entity.Product;
import com.campus.trade.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    // 发布商品
    @PostMapping("/publish")
    public Result<Long> publishProduct(
            @ModelAttribute @Validated PublishProductRequest req,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Long productId = productService.publishProduct(userId, req);
        return Result.success(productId);
    }

    // 商品列表
    @GetMapping("/list")
    public Result<List<Product>> listProducts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Product> list = productService.listProducts(page, size);
        return Result.success(list);
    }

    // 商品详情
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    // 下架商品
    @PutMapping("/offline/{id}")
    public Result<Void> offline(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        productService.offlineProduct(id, userId);
        return Result.success(null);
    }

    // 我的商品（支持分页）
    @GetMapping("/my")
    public Result<Map<String, Object>> myProducts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 参数边界处理
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 50) {
            size = 50;
        }

        // 将状态字符串转换为状态码
        Integer statusCode = null;
        if (status != null && !status.isEmpty()) {
            if ("active".equals(status)) {
                statusCode = 1; // 上架中
            } else if ("sold_out".equals(status)) {
                statusCode = 3; // 已售罄
            } else if ("offline".equals(status)) {
                statusCode = 2; // 已下架
            } else if ("pending".equals(status)) {
                statusCode = 0; // 待审核
            } else if ("rejected".equals(status)) {
                statusCode = 4; // 审核不通过
            }
        }

        // 查询商品列表
        List<Product> products = productService.myProducts(userId, statusCode, page, size);
        
        // 统计总数
        int total = productService.countMyProducts(userId, statusCode);
        int totalPages = (int) Math.ceil((double) total / size);

        Map<String, Object> data = new HashMap<>();
        data.put("products", products);
        data.put("page", page);
        data.put("size", size);
        data.put("total", total);
        data.put("totalPages", totalPages);

        return Result.success(data);
    }

    // 更新商品
    @PostMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id,
            @ModelAttribute PublishProductRequest req,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        productService.updateProduct(userId, id, req);
        return Result.success(null);
    }

    // 搜索商品
    @GetMapping("/search")
    public Result<List<Product>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return Result.success(productService.search(keyword, categoryId, page, size));
    }

    // 删除商品
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        productService.deleteProduct(id, userId);
        return Result.success(null);
    }

    // 待审核商品列表
    @GetMapping("/pending")
    public Result<List<Product>> pendingProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(productService.listPendingProducts(page, size));
    }

    // 审核通过
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        productService.approveProduct(id, userId);
        return Result.success(null);
    }

    // 审核驳回
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id,
            @RequestBody ReviewProductRequest req,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        productService.rejectProduct(id, userId, req.getReason());
        return Result.success(null);
    }
}