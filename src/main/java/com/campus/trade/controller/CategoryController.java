package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.Category;
import com.campus.trade.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取分类列表（只返回一级分类，按sort_order升序排列）
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = categoryService.getActiveCategories();
        return Result.success(categories);
    }

    /**
     * 验证分类ID有效性
     */
    @GetMapping("/validate/{categoryId}")
    public Result<Map<String, Object>> validateCategory(@PathVariable Long categoryId) {
        boolean valid = categoryService.validateCategory(categoryId);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);

        if (valid) {
            Category category = categoryService.getById(categoryId);
            result.put("categoryName", category.getName());
        }

        return Result.success(result);
    }
}