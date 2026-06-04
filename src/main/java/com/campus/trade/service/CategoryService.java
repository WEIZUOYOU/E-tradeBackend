package com.campus.trade.service;

import com.campus.trade.entity.Category;
import com.campus.trade.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * 获取所有启用的一级分类列表
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findActiveCategories();
    }

    /**
     * 验证分类ID是否有效（存在且启用）
     */
    public boolean validateCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return false;
        }
        Category category = categoryRepository.findById(categoryId);
        return category != null && Boolean.TRUE.equals(category.getIsActive());
    }

    /**
     * 根据ID获取分类
     */
    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }
}