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
    `verify_status` INT DEFAULT 0,
    `real_name` VARCHAR(50),
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

-- 学校用户表
CREATE TABLE school_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20),
    real_name VARCHAR(50)
);
-- 导入数据
INSERT INTO school_user (student_id, real_name) VALUES 
('2106010106','杜广奇'),
('2106010125','张垲'),
('2203033218','张雅涵'),
('2207070218','任杰'),
('2301020103','陈银素'),
('2303020201','陈树煊'),
('2303020205','韩倩倩'),
('2304010203','曾正航'),
('2304010218','卢宛娴'),
('2305010202','曾新媛'),
('2305010230','张圯炫'),
('2307010121','王觊'),
('2307010229','伊鼎翔'),
('2307010301','曹家铭'),
('2307010302','曾小红'),
('2307010303','宫雨菲'),
('2307010304','姜季泽'),
('2307010305','李继曈'),
('2307010306','李世文'),
('2307010307','李瑶瑶'),
('2307010308','李奕萱'),
('2307010309','刘鸿涛'),
('2307010310','刘泉灵'),
('2307010311','刘帅'),
('2307010312','吕赵轩'),
('2307010313','乃吉木丁·阿卜拉'),
('2307010314','倪洋'),
('2307010315','钮锌茹'),
('2307010316','秦安阳'),
('2307010315','钮锌茹'),
('2307010316','秦安阳'),
('2307010320','孙昱恒'),
('2307010321','王智卉'),
('2307010322','王梓希'),
('2307010323','魏路源'),
('2307010324','魏浥尘'),
('2307010325','文烈恒'),
('2307010326','熊炳宁'),
('2307010327','杨梓珈弘'),
('2307010328','张博盛'),
('2307010329','张城齐'),
('2307010331','张玥'),
('2307010332','郑张晗'),
('2307010401','阿依则巴·亚合甫江'),
('2307010402','艾尼喀尔·艾克拜尔'),
('2307010404','蔡良昊'),
('2307010405','陈墨'),
('2307010406','陈相源'),
('2307010407','单业'),
('2307010408','董嘉誉'),
('2307010409','方森林'),
('2307010410','郭楷杰'),
('2307010411','侯岳霖'),
('2307010412','矫世桐'),
('2307010413','刘阔'),
('2307010414','刘天琦'),
('2307010419','王博研'),
('2307010420','王畅'),
('2307010415','刘延寒'),
('2307010416','刘宇航'),
('2307010417','马俊宇'),
('2307010418','沙尼亚·萨尔木别克'),
('2307010419','王博研'),
('2307010420','王畅'),
('2307010421','王佳艺'),
('2307010422','王圣博'),
('2307010423','王智妍'),
('2307010424','希嘉妍'),
('2307010425','谢文静'),
('2307010426','杨颜廷'),
('2307010427','余尧'),
('2307010428','庾蕾蕾'),
('2307010429','张世博'),
('2307010431','张宇浩'),
('2307010432','赵珈露'),
('2311010106','葛少杰'),
('2311010205','符美铭'),
('2311020120','武雪妍'),
('2311020210','黄熙凯'),
('2313010126','张建丽');
