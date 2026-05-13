package com.campus.trade.service;

import com.campus.trade.dto.response.FavoriteResponse;
import com.campus.trade.entity.Favorite;
import com.campus.trade.entity.Product;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.FavoriteRepository;
import com.campus.trade.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    // 添加收藏
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否已收藏
        if (favoriteRepository.exists(userId, productId)) {
            throw new BusinessException("已经收藏过该商品");
        }

        favoriteRepository.insert(userId, productId);
    }

    // 取消收藏
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        int rows = favoriteRepository.delete(userId, productId);
        if (rows == 0) {
            throw new BusinessException("收藏不存在或无权操作");
        }
    }

    // 检查收藏状态
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.exists(userId, productId);
    }

    // 获取用户收藏列表（包含商品详情）
    public List<FavoriteResponse> getFavoriteList(Long userId, int page, int size) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId, page, size);
        return favorites.stream().map(favorite -> {
            Product product = productRepository.findById(favorite.getProductId());
            FavoriteResponse resp = new FavoriteResponse();
            resp.setId(favorite.getId());
            resp.setProductId(favorite.getProductId());
            resp.setProduct(product);
            resp.setCreateTime(favorite.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
    }
}
