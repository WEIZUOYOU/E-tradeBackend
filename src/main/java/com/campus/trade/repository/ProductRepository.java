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
        String sql = "SELECT * FROM product WHERE status = 1 ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), size, offset);
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), id)
                .stream().findFirst().orElse(null);
    }

    public Long insert(Product product) {
        String sql = "INSERT INTO product(seller_id, category_id, name, price, stock, description, image_urls, status, view_count, create_time) "
                +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, product.getSellerId());
            ps.setObject(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.setString(6, product.getDescription());
            ps.setString(7, product.getImageUrls());
            ps.setInt(8, product.getStatus() != null ? product.getStatus() : 0);
            ps.setInt(9, product.getViewCount() != null ? product.getViewCount() : 0);
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

    public void incrementViewCount(Long productId) {
        String sql = "UPDATE product SET view_count = view_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, productId);
    }

    // 查询当前用户的商品
    public List<Product> findBySellerId(Long sellerId) {
        String sql = "SELECT * FROM product WHERE seller_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), sellerId);
    }

    // 更新商品信息
    public int updateProduct(Product product) {
        String sql = "UPDATE product SET name=?, category_id=?, price=?, stock=?, description=?, image_urls=?, status=? WHERE id=? AND seller_id=?";
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

    // 搜索商品（关键词 + 分类）
    public List<Product> search(String keyword, Long categoryId, int page, int size) {
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE status = 1");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND name LIKE '%").append(keyword).append("%'");
        }

        if (categoryId != null) {
            sql.append(" AND category_id = ").append(categoryId);
        }

        sql.append(" ORDER BY create_time DESC LIMIT ").append(size).append(" OFFSET ").append(offset);

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Product.class));
    }

    public int delete(Long productId) {
        String sql = "DELETE FROM product WHERE id = ?";
        return jdbcTemplate.update(sql, productId);
    }

    // 分页查询指定状态的商品（审核列表用）
    public List<Product> findByStatus(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM product WHERE status = ? ORDER BY create_time ASC LIMIT ? OFFSET ?";
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
