-- 删除旧数据库（如果存在）
DROP DATABASE IF EXISTS E_tradeDB;
-- 创建新数据库
CREATE DATABASE IF NOT EXISTS E_tradeDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE E_tradeDB;

-- 用户表
CREATE TABLE `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `student_id` VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
CREATE TABLE `category` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(255) COMMENT '分类描述',
    `icon` VARCHAR(500) COMMENT '分类图标',
    `parent_id` INT DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE `product` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `stock` INT NOT NULL DEFAULT 1 COMMENT '库存数量',
    `sold_count` INT DEFAULT 0 COMMENT '已售数量',
    `view_count` INT DEFAULT 0 COMMENT '浏览数量',
    `category_id` INT NOT NULL COMMENT '分类ID',
    `seller_id` INT NOT NULL COMMENT '卖家ID',
    `cover_image` VARCHAR(500) COMMENT '封面图片',
    `images` TEXT COMMENT '商品图片列表（JSON数组）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架，2-已售罄',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
    FOREIGN KEY (seller_id) REFERENCES user(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 收货地址表
CREATE TABLE `address` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省',
    `city` VARCHAR(50) NOT NULL COMMENT '市',
    `district` VARCHAR(50) NOT NULL COMMENT '区/县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 订单表
CREATE TABLE `order` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(32) UNIQUE NOT NULL COMMENT '订单编号',
    `buyer_id` INT NOT NULL COMMENT '买家ID',
    `seller_id` INT NOT NULL COMMENT '卖家ID',
    `product_id` INT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
    `product_image` VARCHAR(500) COMMENT '商品图片（快照）',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价（快照）',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `address_id` INT NOT NULL COMMENT '收货地址ID',
    `receiver_info` TEXT COMMENT '收货地址快照',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
    `pay_time` DATETIME COMMENT '支付时间',
    `deliver_time` DATETIME COMMENT '发货时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (buyer_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (seller_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT,
    FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 购物车表
CREATE TABLE `cart` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `product_id` INT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '商品数量',
    `selected` TINYINT DEFAULT 1 COMMENT '是否选中：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 商品收藏表
CREATE TABLE `favorite` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `product_id` INT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- 消息表
CREATE TABLE `message` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `sender_id` INT NOT NULL COMMENT '发送者ID',
    `receiver_id` INT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT DEFAULT 0 COMMENT '消息类型：0-文本，1-图片，2-系统通知',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
-- 学校用户表
CREATE TABLE school_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20),
    real_name VARCHAR(50)
);
-- 导入数据
INSERT INTO `category` (name, description, icon, parent_id, sort_order) VALUES
('学习用品', '教材、文具、电子设备等', 'book', 0, 1),
('生活用品', '衣物、日用品、收纳等', 'home', 0, 2),
('数码产品', '手机、电脑、配件等', 'laptop', 0, 3),
('运动健身', '运动器材、装备等', 'dumbbell', 0, 4),
('其他物品', '其他未分类物品', 'more', 0, 5);

-- 一级分类下的二级分类
INSERT INTO `category` (name, description, icon, parent_id, sort_order) VALUES
('教材图书', '各类教材、参考书、小说等', NULL, 1, 1),
('文具用品', '笔、本、文具盒等', NULL, 1, 2),
('电子产品', '计算器、耳机、U盘等', NULL, 1, 3),
('衣物鞋帽', '衣服、鞋子、帽子等', NULL, 2, 1),
('日常用品', '水杯、台灯、插排等', NULL, 2, 2),
('装饰品', '挂件、摆件等', NULL, 2, 3);
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
