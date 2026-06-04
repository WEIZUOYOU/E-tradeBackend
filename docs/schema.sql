-- 1. 准备环境
DROP DATABASE IF EXISTS E_tradeDB;
CREATE DATABASE IF NOT EXISTS E_tradeDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE E_tradeDB;

-- 2. 基础表：用户表 (已包含 is_auth 字段)
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 推荐 BIGINT
    `student_id` VARCHAR(20) UNIQUE NULL COMMENT '学号',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `credit_score` INT DEFAULT 100 COMMENT '信用分', -- 补充此处解决Java报错
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `is_auth` TINYINT DEFAULT 0 COMMENT '是否实名认证：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 基础表：商品分类表
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID（NULL表示一级分类）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 插入7个一级分类
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`, `is_active`) VALUES
(1, '教材与学习资料', NULL, 1, 1),
(2, '数码产品与配件', NULL, 2, 1),
(3, '生活电器与宿舍用品', NULL, 3, 1),
(4, '运动与户外', NULL, 4, 1),
(5, '服饰与配饰', NULL, 5, 1),
(6, '美妆与个护', NULL, 6, 1),
(7, '其他/闲置杂物', NULL, 7, 1);

-- 4. 核心表：商品表
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `stock` INT NOT NULL DEFAULT 1 COMMENT '库存',
    `sold_count` INT DEFAULT 0 COMMENT '已售数量',
    `view_count` INT DEFAULT 0 COMMENT '浏览数量',
    `category_id` INT NOT NULL COMMENT '分类ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `cover_image` VARCHAR(500) COMMENT '封面图片',
    `images` TEXT COMMENT '图片列表JSON',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-在售，2-已下架，3-已售出，4-审核不通过',
    `review_reason` VARCHAR(500) COMMENT '审核驳回原因',
    `reviewer_id` INT COMMENT '审核人ID',
    `reviewed_time` DATETIME COMMENT '审核时间',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '推荐：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (seller_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 5. 交易表：地址表
CREATE TABLE `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `receiver_name` VARCHAR(50) NOT NULL,
    `receiver_phone` VARCHAR(20) NOT NULL,
    `province` VARCHAR(50),
    `city` VARCHAR(50),
    `district` VARCHAR(50),
    `detail_address` VARCHAR(255) NOT NULL,
    `is_default` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';

-- 6. 核心表：订单表 (已集成线下交易字段)
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(32) UNIQUE NOT NULL,
    `buyer_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(100),
    `product_image` VARCHAR(500),
    `product_price` DECIMAL(10,2),
    `quantity` INT DEFAULT 1,
    `total_amount` DECIMAL(10,2),
    `address_id` BIGINT DEFAULT NULL COMMENT '线下交易时可为空',  -- 修复为可为空
    `trade_type` TINYINT DEFAULT 1 COMMENT '0-快递, 1-线下交易',
    `meeting_time` DATETIME COMMENT '线下约定时间',
    `meeting_location` VARCHAR(255) COMMENT '线下约定地点',
    `status` TINYINT DEFAULT 0 COMMENT '0-待支付/确认, 1-已支付/交易中, 2-已发货/交付, 3-已完成, 4-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES user(id),
    FOREIGN KEY (seller_id) REFERENCES user(id)
    -- 移除了 product_id 和 address_id 的外键限制，真正实现“快照”解耦
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 7. 互动表：购物车、收藏、消息、历史记录
CREATE TABLE `cart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT DEFAULT 1,
    `selected` TINYINT DEFAULT 1,
    UNIQUE KEY uk_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `favorite` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `message` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `is_read` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `browse_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `browse_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 管理表：信用、评价、实名、举报、学校库
CREATE TABLE `user_credit` (
    `user_id` BIGINT PRIMARY KEY,
    `credit_score` INT DEFAULT 100,
    `trade_count` INT DEFAULT 0,
    `good_review_rate` DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_review` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `reviewer_id` BIGINT NOT NULL,
    `reviewee_id` BIGINT NOT NULL,
    `review_type` TINYINT NOT NULL COMMENT '0-买家评价卖家, 1-卖家评价买家',
    `rating` TINYINT COMMENT '1-5星',
    `content` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `authentication` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `real_name` VARCHAR(50) NOT NULL,
    `student_id` VARCHAR(20) NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核, 1-通过, 2-拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `report` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `reporter_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `reason` VARCHAR(255),
    `status` TINYINT DEFAULT 0 COMMENT '0-未处理, 1-已处理, 2-已驳回',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handle_result` VARCHAR(255) COMMENT '处理结果说明',
    `handle_time` DATETIME COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '接收者ID',
    `type` TINYINT DEFAULT 0 COMMENT '0-系统通知, 1-举报处理, 2-审核结果',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `related_id` BIGINT COMMENT '关联业务ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '0-未读, 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `school_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `student_id` VARCHAR(20) UNIQUE,
    `real_name` VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 导入学校基础数据 (已去重)
INSERT INTO school_user (student_id, real_name) VALUES 
('2106010106','杜广奇'),('2106010125','张垲'),('2203033218','张雅涵'),
('2207070218','任杰'),('2301020103','陈银素'),('2303020201','陈树煊'),
('2303020205','韩倩倩'),('2304010203','曾正航'),('2304010218','卢宛娴'),
('2305010202','曾新媛'),('2305010230','张圯炫'),('2307010121','王觊'),
('2307010229','伊鼎翔'),('2307010301','曹家铭'),('2307010302','曾小红'),
('2307010303','宫雨菲'),('2307010304','姜季泽'),('2307010305','李继曈'),
('2307010306','李世文'),('2307010307','李瑶瑶'),('2307010308','李奕萱'),
('2307010309','刘鸿涛'),('2307010310','刘泉灵'),('2307010311','刘帅'),
('2307010312','吕赵轩'),('2307010313','乃吉木丁·阿卜拉'),('2307010314','倪洋'),
('2307010315','钮锌茹'),('2307010316','秦安阳'),('2307010320','孙昱恒'),
('2307010321','王智卉'),('2307010322','王梓希'),('2307010323','魏路源'),
('2307010324','魏浥尘'),('2307010325','文烈恒'),('2307010326','熊炳宁'),
('2307010327','杨梓珈弘'),('2307010328','张博盛'),('2307010329','张城齐'),
('2307010331','张玥'),('2307010332','郑张晗'),('2307010401','阿依则巴·亚合甫江'),
('2307010402','艾尼喀尔·艾克拜尔'),('2307010404','蔡良昊'),('2307010405','陈墨'),
('2307010406','陈相源'),('2307010407','单业'),('2307010408','董嘉誉'),
('2307010409','方森林'),('2307010410','郭楷杰'),('2307010411','侯岳霖'),
('2307010412','矫世桐'),('2307010413','刘阔'),('2307010414','刘天琦'),
('2307010415','刘延寒'),('2307010416','刘宇航'),('2307010417','马俊宇'),
('2307010418','沙尼亚·萨尔木别克'),('2307010419','王博研'),('2307010420','王畅'),
('2307010421','王佳艺'),('2307010422','王圣博'),('2307010423','王智妍'),
('2307010424','希嘉妍'),('2307010425','谢文静'),('2307010426','杨颜廷'),
('2307010427','余尧'),('2307010428','庾蕾蕾'),('2307010429','张世博'),
('2307010431','张宇浩'),('2307010432','赵珈露'),('2311010106','葛少杰'),
('2311010205','符美铭'),('2311020120','武雪妍'),('2311020210','黄熙凯'),
('2313010126','张建丽');