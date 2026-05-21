package com.campus.trade.service;

import com.campus.trade.dto.request.CreateCategoryRequest;
import com.campus.trade.entity.Category;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllActive();
    }

    public Long createCategory(CreateCategoryRequest req) {
        if (!StringUtils.hasText(req.getName())) {
            throw new BusinessException("分类名称不能为空");
        }

        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setIcon(req.getIcon());
        category.setParentId(req.getParentId() != null ? req.getParentId() : 0);
        category.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        category.setStatus(req.getStatus() != null ? req.getStatus() : 1);

        return categoryRepository.insert(category);
    }
}