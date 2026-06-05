# 校园二手交易平台 - 后端

基于 Spring Boot 3.5.13 + JDBC Template 的校园二手交易平台后端，提供用户认证、商品管理、审核管理、订单交易、举报处理、消息通知等功能。

## 技术栈

- Spring Boot 3.5.13
- Spring Web (RESTful API)
- Spring JDBC Template
- MySQL 8.0+
- Lombok
- Spring Security Crypto (BCrypt)
- Maven 3.6+
- JDK 21

## 快速启动

### 1. 环境要求
- JDK 21
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
执行 `docs/schema.sql` 文件创建数据库以及所有表结构。

### 3. 配置文件
编辑 `src/main/resources/application.yml`，修改数据库密码：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/E_tradeDB?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true
    username: root
    password: your_password

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

file:
  upload-dir: ./uploads/
```

### 4. 运行项目

```bash
# 更新数据库
USE E_tradeDB;
source D:/E-tradeBackend/docs/schema.sql; # 执行数据库初始化脚本

# 运行项目
mvn spring-boot:run
```

## 完整 API 文档

详见根目录 `api.txt`，共 42 个接口，按模块分类：

| 模块 | 接口路径 | 数量 |
|------|---------|------|
| 用户模块 | `/api/user/*` | 12 |
| 商品模块 | `/api/product/*` | 11 |
| 订单模块 | `/api/v1/trade/order/*` | 8 |
| 分类模块 | `/api/category/*` | 1 |
| 消息模块 | `/api/message/*` | 2 |
| 举报模块 | `/api/v1/trade/report/*` | 4 |
| 通知模块 | `/api/notification/*` | 4 |

## 主要功能

- **用户模块**：注册、登录（手机号）、实名认证（学校预置库比对）、头像上传、资料修改
- **用户管理**：冻结/解冻账号、重置密码、用户列表、待认证列表
- **商品模块**：发布（多图上传）、列表、详情、搜索、下架、删除、修改
- **商品审核**：待审核列表、审核通过、审核驳回（含驳回原因）
- **订单模块**：创建订单（减库存）、付款/发货/收货状态流转、取消
- **举报模块**：提交举报、举报列表、处理（下架+封号+通知）、驳回
- **通知模块**：系统通知、未读计数、已读标记、微信模板消息骨架
- **消息模块**：买卖双方即时通讯

## 接口示例

### 注册
```http
POST /api/user/register
Content-Type: application/json

{
  "studentId": "2307010301",
  "username": "张三",
  "password": "123456",
  "phone": "13812345678"
}
```

### 登录（使用手机号）
```http
POST /api/user/login
Content-Type: application/json

{
  "phone": "13812345678",
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
description: 九成新
categoryId: 1
images: [file1.jpg, file2.jpg]
```

### 商品列表
```http
GET /api/product/list?page=1&size=10
```

### 搜索商品
```http
GET /api/product/search?keyword=教材&categoryId=1&page=1&size=10
```

### 创建订单
```http
POST /api/v1/trade/order
Content-Type: application/json
Cookie: JSESSIONID=xxx

{
  "productId": 1,
  "quantity": 1,
  "tradeType": 0,
  "addressId": 10
}
```

### 审核商品
```http
PUT /api/product/approve/1               # 审核通过
PUT /api/product/reject/1                # 审核驳回
Content-Type: application/json
{ "reason": "图片不符合规范" }
```

### 处理举报
```http
PUT /api/v1/trade/report/1/handle        # 处理（下架商品+封号+通知）
PUT /api/v1/trade/report/1/dismiss       # 驳回
```

## 目录结构

```
E-tradeBackend-master/
├── docs/
│   └── schema.sql                              # 数据库初始化脚本
├── src/main/java/com/campus/trade/
│   ├── TradeApplication.java                   # 启动类
│   ├── common/
│   │   ├── Result.java                         # 统一响应体
│   │   ├── ResultCode.java                     # 响应状态码枚举
│   │   └── constants/
│   │       ├── OrderStatus.java                # 订单状态常量
│   │       └── TradeType.java                  # 交易类型常量
│   ├── config/
│   │   └── WebConfig.java                      # Web配置（CORS）
│   ├── controller/
│   │   ├── UserController.java                 # 用户接口 (12个)
│   │   ├── ProductController.java              # 商品接口 (11个)
│   │   ├── OrderController.java                # 订单接口 (8个)
│   │   ├── CategoryController.java             # 分类接口
│   │   ├── MessageController.java              # 消息接口
│   │   ├── ReportController.java               # 举报接口 (4个)
│   │   └── NotificationController.java         # 通知接口 (4个)
│   ├── service/
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   ├── OrderService.java
│   │   ├── CategoryService.java
│   │   ├── MessageService.java
│   │   ├── ReportService.java
│   │   └── NotificationService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── OrderRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── MessageRepository.java
│   │   ├── ReportRepository.java
│   │   ├── NotificationRepository.java
│   │   └── SchoolUserRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── Category.java
│   │   ├── Message.java
│   │   ├── Report.java
│   │   └── Notification.java
│   ├── dto/
│   │   ├── ReviewProductRequest.java
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── PublishProductRequest.java
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── CreateReportRequest.java
│   │   │   ├── SendMessageRequest.java
│   │   │   ├── VerifyRequest.java
│   │   │   ├── UpdateProfileRequest.java
│   │   │   └── UserManagementRequest.java
│   │   └── response/
│   │       ├── OrderDetailResponse.java
│   │       ├── ReportResponse.java
│   │       └── CategoryResponse.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── BusinessException.java
│   ├── interceptor/
│   │   └── LoginInterceptor.java
│   └── utils/
│       ├── FileUploadUtils.java
│       ├── PasswordUtil.java
│       └── WeChatNotifyUtil.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
├── struct.txt                                  # 项目结构说明
├── api.txt                                     # 完整API文档
└── README.md
```

## 数据库设计要点

- **user** — 用户表（含信用分、实名认证标识、冻结状态）
- **product** — 商品表（含审核状态、驳回原因、审核人）
- **order** — 订单表（支持线上/线下交易，快照机制）
- **category** — 分类表（支持父子级联）
- **message** — 私信表（买卖双方即时通讯）
- **report** — 举报表（含处理人和处理结果）
- **notification** — 通知表（站内通知，待微信推送打通）
- **school_user** — 学校预置库（实名认证比对底库）
