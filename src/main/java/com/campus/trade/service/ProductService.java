package com.campus.trade.service;

import com.campus.trade.dto.request.PublishProductRequest;
import com.campus.trade.entity.Product;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.ProductRepository;
import com.campus.trade.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 发布商品（含图片上传）
    public Long publishProduct(Long sellerId, PublishProductRequest req) {
        // 验证分类ID
        if (!categoryService.validateCategory(req.getCategoryId())) {
            throw new BusinessException("无效的商品分类");
        }

        // 处理图片上传
        List<MultipartFile> images = req.getImages();
        if (images == null || images.isEmpty()) {
            throw new BusinessException("至少上传一张图片");
        }
        List<String> savedPaths = images.stream().map(file -> {
            try {
                return FileUploadUtils.saveFile(uploadDir, file);
            } catch (IOException e) {
                throw new BusinessException("图片上传失败: " + e.getMessage());
            }
        }).collect(Collectors.toList());

        String imageUrls = String.join(",", savedPaths);

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setCategoryId(req.getCategoryId());
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setDescription(req.getDescription());
        product.setImages(savedPaths);
        product.setStatus(0);// 0-待审核
        product.setViewCount(0);

        return productRepository.insert(product);
    }

    // 商品列表（分页）
    public List<Product> listProducts(int page, int size) {
        return productRepository.findAll(page, size);
    }

    // 商品详情
    public Product getProductDetail(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        // 增加浏览量（简单处理，不计入频繁更新）
        productRepository.incrementViewCount(productId);
        return product;
    }

    // 我的商品
    public List<Product> myProducts(Long userId) {
        return productRepository.findBySellerId(userId);
    }

    // 更新商品
    public void updateProduct(Long userId, Long productId, PublishProductRequest req) {

        Product old = productRepository.findById(productId);
        if (old == null) {
            throw new BusinessException("商品不存在");
        }

        if (!old.getSellerId().equals(userId)) {
            throw new BusinessException("无权限");
        }

        // 验证分类ID
        if (req.getCategoryId() != null && !categoryService.validateCategory(req.getCategoryId())) {
            throw new BusinessException("无效的商品分类");
        }

        // 图片处理（可选）
        List<String> savedPaths = null;
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            savedPaths = req.getImages().stream().map(file -> {
                try {
                    return FileUploadUtils.saveFile(uploadDir, file);
                } catch (Exception e) {
                    throw new BusinessException("图片上传失败");
                }
            }).toList();
        }

        Product product = new Product();
        product.setId(productId);
        product.setSellerId(userId);
        product.setName(req.getName());
        product.setCategoryId(req.getCategoryId());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setDescription(req.getDescription());

        // 使用 setImages() 方法（适配前端）
        if (savedPaths != null) {
            product.setImages(savedPaths);
        }

        // 重新上架
        product.setStatus(0);

        int rows = productRepository.updateProduct(product);
        if (rows == 0) {
            throw new BusinessException("更新失败");
        }
    }

    // 下架商品
    public void offlineProduct(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        int rows = productRepository.updateStatus(productId, 2); // 2-已下架
        if (rows == 0) {
            throw new BusinessException("下架失败");
        }
    }

    // 搜索
    public List<Product> search(String keyword, Long categoryId, int page, int size) {
        return productRepository.search(keyword, categoryId, page, size);
    }

    // 删除商品
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        int rows = productRepository.delete(productId);
        if (rows == 0) {
            throw new BusinessException("删除失败");
        }
    }

    // 待审核商品列表
    public List<Product> listPendingProducts(int page, int size) {
        return productRepository.findByStatus(0, page, size);
    }

    // 审核通过
    public void approveProduct(Long productId, Long reviewerId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 0) {
            throw new BusinessException("商品非待审核状态");
        }
        int rows = productRepository.approveProduct(productId, reviewerId);
        if (rows == 0) {
            throw new BusinessException("审核操作失败，商品状态可能已变更");
        }
    }

    // 审核驳回
    public void rejectProduct(Long productId, Long reviewerId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("驳回时必须填写原因");
        }
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 0) {
            throw new BusinessException("商品非待审核状态");
        }
        int rows = productRepository.rejectProduct(productId, reviewerId, reason.trim());
        if (rows == 0) {
            throw new BusinessException("审核操作失败，商品状态可能已变更");
        }
    }
}