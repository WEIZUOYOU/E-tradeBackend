package com.campus.trade.repository;

import com.campus.trade.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Product> findAll(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT p.*, u.username AS seller_name, u.avatar AS seller_avatar, u.is_auth AS seller_is_auth, c.name AS category_name " +
                     "FROM product p " +
                     "LEFT JOIN user u ON p.seller_id = u.id " +
                     "LEFT JOIN category c ON p.category_id = c.id " +
                     "WHERE p.status = 1 ORDER BY p.create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), size, offset);
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), id)
                .stream().findFirst().orElse(null);
    }

    /**
     * 获取商品详情（含卖家信息和分类名称）
     */
    public Product findByIdWithSeller(Long id) {
        String sql = "SELECT p.*, u.username AS seller_name, u.avatar AS seller_avatar, u.is_auth AS seller_is_auth, c.name AS category_name " +
                     "FROM product p " +
                     "LEFT JOIN user u ON p.seller_id = u.id " +
                     "LEFT JOIN category c ON p.category_id = c.id " +
                     "WHERE p.id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), id)
                .stream().findFirst().orElse(null);
    }

    public Long insert(Product product) {
        String sql = "INSERT INTO product(seller_id, category_id, name, price, stock, sold_count, description, images, cover_image, status, view_count, is_recommend, create_time) "
                +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, product.getSellerId());
            ps.setObject(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.setInt(6, product.getSoldCount() != null ? product.getSoldCount() : 0);
            ps.setString(7, product.getDescription());
            ps.setString(8, product.getImageUrls());
            ps.setString(9, product.getCoverImage());
            ps.setInt(10, product.getStatus() != null ? product.getStatus() : 0);
            ps.setInt(11, product.getViewCount() != null ? product.getViewCount() : 0);
            ps.setInt(12, product.getIsRecommend() != null ? product.getIsRecommend() : 0);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public int updateStatus(Long productId, Integer status) {
        String sql = "UPDATE product SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, productId);
    }

    public int deductStock(Long productId, Integer quantity) {
        String sql = "UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?";
        return jdbcTemplate.update(sql, quantity, productId, quantity);
    }

    /**
     * 恢复库存（取消订单时使用）
     */
    public int restoreStock(Long productId, Integer quantity) {
        String sql = "UPDATE product SET stock = stock + ? WHERE id = ?";
        return jdbcTemplate.update(sql, quantity, productId);
    }

    public void incrementViewCount(Long productId) {
        String sql = "UPDATE product SET view_count = view_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, productId);
    }

    /**
     * 查询当前用户的商品（分页，含卖家信息和分类名称）
     * @param sellerId 卖家ID
     * @param status 商品状态（可选）
     * @param page 页码（从1开始）
     * @param size 每页数量
     */
    public List<Product> findBySellerIdWithPage(Long sellerId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        StringBuilder sql = new StringBuilder("SELECT p.*, u.username AS seller_name, u.avatar AS seller_avatar, u.is_auth AS seller_is_auth, c.name AS category_name " +
                                              "FROM product p " +
                                              "LEFT JOIN user u ON p.seller_id = u.id " +
                                              "LEFT JOIN category c ON p.category_id = c.id " +
                                              "WHERE p.seller_id = ?");
        
        if (status != null) {
            sql.append(" AND p.status = ?");
            return jdbcTemplate.query(sql.toString() + " ORDER BY p.create_time DESC LIMIT ? OFFSET ?", 
                new BeanPropertyRowMapper<>(Product.class), sellerId, status, size, offset);
        } else {
            return jdbcTemplate.query(sql.toString() + " ORDER BY p.create_time DESC LIMIT ? OFFSET ?", 
                new BeanPropertyRowMapper<>(Product.class), sellerId, size, offset);
        }
    }

    /**
     * 统计当前用户的商品数量
     * @param sellerId 卖家ID
     * @param status 商品状态（可选）
     */
    public int countBySellerId(Long sellerId, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM product WHERE seller_id = ?");
        
        if (status != null) {
            sql.append(" AND status = ?");
            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, sellerId, status);
            return count != null ? count : 0;
        } else {
            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, sellerId);
            return count != null ? count : 0;
        }
    }

    /**
     * 查询当前用户的商品（按状态筛选）
     * @param sellerId 卖家ID
     * @param status 商品状态：1-上架中（active），3-已售罄（sold_out）
     */
    public List<Product> findBySellerIdAndStatus(Long sellerId, Integer status) {
        String sql = "SELECT p.*, u.username AS seller_name, u.avatar AS seller_avatar, u.is_auth AS seller_is_auth, c.name AS category_name " +
                     "FROM product p " +
                     "LEFT JOIN user u ON p.seller_id = u.id " +
                     "LEFT JOIN category c ON p.category_id = c.id " +
                     "WHERE p.seller_id = ?";
        
        if (status != null) {
            sql += " AND p.status = ?";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), sellerId, status);
        } else {
            sql += " ORDER BY p.create_time DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), sellerId);
        }
    }

    // 更新商品信息
    public int updateProduct(Product product) {
        String sql = "UPDATE product SET name=?, category_id=?, price=?, stock=?, description=?, images=?, status=? WHERE id=? AND seller_id=?";
        return jdbcTemplate.update(sql,
                product.getName(),
                product.getCategoryId(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getImageUrls(),
                product.getStatus(),
                product.getId(),
                product.getSellerId());
    }

    // 搜索商品（关键词 + 分类，含卖家信息）
    public List<Product> search(String keyword, Long categoryId, int page, int size) {
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("SELECT p.*, u.username AS seller_name, u.avatar AS seller_avatar, u.is_auth AS seller_is_auth, c.name AS category_name " +
                                              "FROM product p LEFT JOIN user u ON p.seller_id = u.id LEFT JOIN category c ON p.category_id = c.id WHERE p.status = 1");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND p.name LIKE '%").append(keyword).append("%'");
        }

        if (categoryId != null) {
            sql.append(" AND p.category_id = ").append(categoryId);
        }

        sql.append(" ORDER BY p.create_time DESC LIMIT ").append(size).append(" OFFSET ").append(offset);

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Product.class));
    }

    public int delete(Long productId) {
        String sql = "DELETE FROM product WHERE id = ?";
        return jdbcTemplate.update(sql, productId);
    }

    // 分页查询指定状态的商品（审核列表用，含卖家信息和分类名称）
    public List<Product> findByStatus(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT p.*, u.username AS seller_name, u.avatar AS seller_avatar, u.is_auth AS seller_is_auth, c.name AS category_name " +
                     "FROM product p " +
                     "LEFT JOIN user u ON p.seller_id = u.id " +
                     "LEFT JOIN category c ON p.category_id = c.id " +
                     "WHERE p.status = ? ORDER BY p.create_time ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), status, size, offset);
    }

    // 审核通过：status 0→1，记录审核人和时间
    public int approveProduct(Long productId, Long reviewerId) {
        String sql = "UPDATE product SET status = 1, reviewer_id = ?, reviewed_time = NOW() WHERE id = ? AND status = 0";
        return jdbcTemplate.update(sql, reviewerId, productId);
    }

    // 审核驳回：status 0→4，记录驳回原因、审核人和时间
    public int rejectProduct(Long productId, Long reviewerId, String reason) {
        String sql = "UPDATE product SET status = 4, review_reason = ?, reviewer_id = ?, reviewed_time = NOW() WHERE id = ? AND status = 0";
        return jdbcTemplate.update(sql, reason, reviewerId, productId);
    }
}
