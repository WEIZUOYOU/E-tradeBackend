-- 删除旧数据库（如果存在）
DROP DATABASE IF EXISTS E_tradeDB;
-- 创建新数据库
CREATE DATABASE IF NOT EXISTS E_tradeDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 使用数据库
USE E_tradeDB;

-- 用户表
CREATE TABLE `user` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `student_id` varchar(20) NOT NULL UNIQUE,
    `username` varchar(20) NOT NULL,
    `password` varchar(100) NOT NULL,
    `phone` varchar(20),
    `avatar` varchar(255),
    `credit_score` int DEFAULT 100,
    `status` tinyint DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品表（支持多图）
CREATE TABLE `product` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `seller_id` bigint NOT NULL,
    `category_id` bigint,
    `name` varchar(50) NOT NULL,
    `price` decimal(10,2) NOT NULL,
    `stock` int NOT NULL,
    `description` varchar(500),
    `image_urls` text COMMENT '多张图片URL，逗号分隔', 
    `status` tinyint DEFAULT 0,
    `view_count` int DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收货地址表
CREATE TABLE `address` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `contact` varchar(50) NOT NULL,           -- 收货人姓名
    `phone` varchar(20) NOT NULL,             -- 收货人电话
    `detail_address` varchar(200) NOT NULL,   -- 详细地址
    `is_default` tinyint DEFAULT 0,           -- 是否默认地址
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表（含地址和物流字段）
CREATE TABLE `order` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `order_no` varchar(32) NOT NULL UNIQUE,
    `buyer_id` bigint NOT NULL,
    `seller_id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    `product_name` varchar(50) NOT NULL,
    `product_price_at_order` decimal(10,2) NOT NULL,
    `quantity` int NOT NULL,
    `total_amount` decimal(10,2) NOT NULL,
    `address_id` bigint,                     -- 关联地址表
    `logistics_company` varchar(50),
    `tracking_no` varchar(50),
    `status` tinyint DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `pay_time` datetime,
    `complete_time` datetime
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;