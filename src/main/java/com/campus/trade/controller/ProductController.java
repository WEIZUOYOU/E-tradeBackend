package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.PublishProductRequest;
import com.campus.trade.entity.Product;
import com.campus.trade.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;

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

    // 我的商品
    @GetMapping("/my")
    public Result<List<Product>> myProducts(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(productService.myProducts(userId));
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
}