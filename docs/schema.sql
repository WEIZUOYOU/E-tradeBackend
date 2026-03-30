-- 用户表
CREATE TABLE `t_user` (
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

-- 商品表
CREATE TABLE `t_product` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `seller_id` bigint NOT NULL,
    `category_id` bigint,
    `name` varchar(50) NOT NULL,
    `price` decimal(10,2) NOT NULL,
    `stock` int NOT NULL,
    `description` varchar(500),
    `image_urls` text,
    `status` tinyint DEFAULT 0,
    `view_count` int DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE `t_order` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `order_no` varchar(32) NOT NULL UNIQUE,
    `buyer_id` bigint NOT NULL,
    `seller_id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    `product_name` varchar(50) NOT NULL,
    `product_price_at_order` decimal(10,2) NOT NULL,
    `quantity` int NOT NULL,
    `total_amount` decimal(10,2) NOT NULL,
    `status` tinyint DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `pay_time` datetime,
    `complete_time` datetime
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;