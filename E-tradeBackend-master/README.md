# 校园二手交易平台 - 后端

基于 Spring Boot 3.5.13 + JDBC Template 的校园二手交易平台后端，提供用户认证、商品管理、订单交易等基础功能。

## 技术栈

- Spring Boot 3.5.13
- Spring Web
- Spring JDBC Template
- MySQL 8.0.33
- Lombok
- Maven 3.6+

## 快速启动

### 1. 环境要求
- JDK 21
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
执行 `docs/schema.sql` 文件创建数据库以及所有表结构。

### 3. 配置文件修改
编辑 `src/main/resources/application.yml`，根据实际情况调整以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/E_tradeDB?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: your_password   # 修改为你的MySQL数据库密码

  servlet:
    multipart:
      max-file-size: 10MB     # 文件上传大小限制

file:
  upload-dir: ./src/main/resources/static/upload   # 图片上传目录（可自定义）

server:
  port: 8080                  # 服务端口，可按需修改
```

> **说明**：  
> - 数据库名必须为 `E_tradeDB`，与 `schema.sql` 保持一致。  
> - 上传目录 `file.upload-dir` 需要与 `WebConfig` 中的静态资源映射路径匹配，确保上传的图片可通过 `http://localhost:8080/upload/**` 访问。

###  运行项目
# 1. 编译打包
mvn clean package -DskipTests

# 2. 运行
java -jar target/campus-trade-1.0.0.jar

# 3. 带配置文件运行
java -jar target/campus-trade-1.0.0.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/E_tradeDB \
  --spring.datasource.username=root \
  --spring.datasource.password=your_password \
  --server.port=8080


服务启动后，接口根路径为 `http://localhost:8080/api/...`。

## 主要功能

- **用户模块**：注册、登录、退出、获取当前用户
- **商品模块**：发布（含多图上传）、列表、详情、下架
- **订单模块**：创建订单（原子扣减库存，关联收货地址）

## 接口示例

### 注册
```http
POST /api/user/register
Content-Type: application/json

{
  "studentId": "20210001",
  "username": "张三",
  "password": "123456",
  "phone": "13812345678"
}
```

### 登录
```http
POST /api/user/login
Content-Type: application/json

{
  "studentId": "20210001",
  "password": "123456"
}
```

### 发布商品
```http
POST /api/product/publish
Content-Type: multipart/form-data
Cookie: JSESSIONID=xxx

name: 二手教材
price: 20.00
stock: 1
description: 九成新，无笔记
categoryId: 1
images: file1.jpg, file2.jpg   # 支持多图，前端需以数组形式上传
```

### 商品列表
```http
GET /api/product/list?page=1&size=10&categoryId=1
```

### 创建订单
```http
POST /api/order/create
Content-Type: application/json
Cookie: JSESSIONID=xxx

{
  "productId": 1,
  "quantity": 1,
  "addressId": 10    # 收货地址ID
}
```

## 目录结构

```
campus-trade-backend/
├── src/main/java/com/campus/trade/
│   ├── TradeApplication.java               # 启动类
│   ├── config/                             # 配置类
│   │   ├── WebConfig.java                  # Web配置（拦截器、跨域、静态资源映射）
│   │   └── DatabaseConfig.java             # 数据源配置
│   ├── interceptor/                        # 拦截器
│   │   └── LoginInterceptor.java           # 登录拦截器
│   ├── controller/                         # 控制器层
│   │   ├── UserController.java             # 用户相关接口
│   │   ├── ProductController.java          # 商品相关接口
│   │   ├── OrderController.java            # 订单相关接口
│   │   ├── CategoryController.java         # 分类相关接口
│   │   └── UploadController.java           # 文件上传接口
│   ├── service/                            # 服务层
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   ├── OrderService.java
│   │   ├── CategoryService.java
│   │   └── FileUploadService.java
│   ├── repository/                         # 数据访问层（JdbcTemplate）
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── OrderRepository.java
│   │   ├── CategoryRepository.java
│   │   └── AddressRepository.java
│   ├── entity/                             # 实体类
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── Category.java
│   │   └── Address.java
│   ├── dto/                                # 数据传输对象
│   │   ├── request/                        # 请求DTO
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── ProductPublishRequest.java
│   │   │   ├── OrderCreateRequest.java
│   │   │   └── AddressRequest.java
│   │   └── response/                       # 响应DTO
│   │       ├── UserResponse.java
│   │       ├── ProductResponse.java
│   │       ├── OrderResponse.java
│   │       └── AddressResponse.java
│   ├── common/                             # 公共类
│   │   ├── Result.java                     # 统一响应封装
│   │   ├── PageResult.java                 # 分页响应
│   │   └── constants/                      # 常量类
│   │       ├── UserStatus.java             # 用户状态
│   │       ├── ProductStatus.java          # 商品状态
│   │       └── OrderStatus.java            # 订单状态
│   ├── exception/                          # 异常处理
│   │   ├── GlobalExceptionHandler.java     # 全局异常处理
│   │   └── BusinessException.java          # 业务异常
│   └── utils/                              # 工具类
│       ├── FileUploadUtil.java             # 文件上传工具
│       ├── JwtUtil.java                    # JWT工具（可选）
│       └── PasswordUtil.java              # 密码加密工具
├── src/main/resources/
│   ├── application.yml                     # 应用配置文件
│   └── static/                             # 静态资源
│       └── upload/                         # 上传文件目录
├── docs/
│   └── schema.sql                          # 数据库初始化脚本
├── pom.xml                                 # Maven配置文件
└── README.md                               # 项目说明文档

sql表：
```
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
```


c```
性能优化建议

数据库优化

添加合适的索引

使用连接池（HikariCP已配置）

读写分离（后续扩展）

缓存优化

商品列表添加Redis缓存

用户Session使用Redis存储

接口优化

添加分页查询

批量操作接口

异步处理
```

## 1 短期优化

添加商品搜索功能（Elasticsearch）

实现消息通知系统（WebSocket）

添加商品收藏功能

实现购物车功能

添加商品评论和评分

## 2 中期规划

集成支付功能（微信/支付宝）

实现物流跟踪

添加数据统计和分析

实现推荐算法

移动端适配

## 3 长期演进

微服务架构改造

分布式事务处理

大数据分析平台

AI智能推荐

跨平台支持