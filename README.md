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

🛡️ 核心用户模块
1. 用户表 (user)
存储账号基础信息，增加了实名认证标识。

关键字段：student_id (唯一), is_auth (实名标识)。

2. 学校预存用户库 (school_user)
用于实名认证比对的“底库”，存储全校学生的基础信息。

3. 实名认证记录表 (authentication)
记录用户的认证申请、审核状态及上传的证件照片。

4. 用户信用档案表 (user_credit)
存储信用分、成交次数和好评率，随交易完成动态更新。

📦 商品管理模块
5. 商品分类表 (category)
支持无限级分类（通过 parent_id），通常用于首页导航。

6. 商品表 (product)
核心表，存储单价、库存、封面及图片列表（JSON格式）。

外键：关联 category 和 seller_id (user)。

7. 商品浏览记录表 (browse_history)
记录用户“看过”的商品，用于实现“我的足迹”。

8. 商品收藏表 (favorite)
记录用户收藏的商品，采用复合唯一索引 uk_user_product 防止重复收藏。

🤝 交易与订单模块
9. 订单表 (order)
支持校园特色的线下交易。

新增字段：trade_type (0-邮寄, 1-线下), meeting_time, meeting_location。

快照机制：存储 product_name 等快照，防止商品删除后订单数据显示异常。

10. 收货地址表 (address)
存储用户的常用收货信息。

11. 购物车表 (cart)
存储待下单的商品及数量。

12. 订单评价表 (order_review)
交易完成后，买家对卖家的评分及文字图片反馈。

💬 互动与系统模块
13. 消息表 (message)
支持买卖双方的私信（聊一聊），支持文字和图片。

14. 举报管理表 (report)
用于维护校园交易环境，处理违规商品举报。


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