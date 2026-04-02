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

### 4. 运行项目

#### 方式一：Maven 直接运行
```bash
mvn spring-boot:run
```

#### 方式二：打包后运行
```bash
mvn clean package
java -jar target/campus-trade-1.0-SNAPSHOT.jar
```

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
src/main/java/com/campus/trade/
├── TradeApplication.java        # 启动类
├── config/                      # 配置类（拦截器、静态资源映射）
├── interceptor/                 # 登录拦截器
├── controller/                  # 控制器
├── service/                     # 业务逻辑
├── repository/                  # 数据访问层（JdbcTemplate）
├── entity/                      # 实体类
├── dto/                         # 请求/响应数据传输对象
├── common/                      # 统一响应封装
├── exception/                   # 全局异常处理
└── utils/                       # 工具类（文件上传等）
```

## 后续拓展建议

- 引入 Redis 缓存热点数据
- 使用 JWT 代替 Session 实现无状态认证
- 集成对象存储（如阿里云 OSS）代替本地存储
- 增加商品分类、搜索、消息通知等功能
- 完善订单状态流转与支付对接
- 添加单元测试与集成测试
