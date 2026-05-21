package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.CreateCategoryRequest;
import com.campus.trade.entity.Category;
import com.campus.trade.service.CategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getAllCategories());
    }

    @PostMapping("/add")
    public Result<Long> add(@Validated @RequestBody CreateCategoryRequest req) {
        return Result.success(categoryService.createCategory(req));
    }
}