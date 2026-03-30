# 校园二手交易平台 - 后端

基于 Spring Boot 2.7 + JDBC Template 的校园二手交易平台后端，提供用户认证、商品管理、订单交易等基础功能。

## 技术栈

- Spring Boot 2.7.18
- Spring Web
- Spring JDBC Template
- MySQL 8.0
- Lombok
- Maven

## 快速启动

### 1. 环境要求
- JDK 11+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
创建数据库 `campus_trade`，然后执行schema.sql文件创建表结构

### 3. 配置修改
编辑 `src/main/resources/application.yml`，修改数据库连接信息、文件上传目录。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trade?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 修改为你的数据库密码

file:
  upload-dir: ./src/main/resources/static/upload  # 文件上传目录
```

### 4. 运行项目
```bash
mvn spring-boot:run
```

服务启动后，接口根路径为 `http://localhost:8080/api/...`

## 主要功能

- **用户模块**：注册、登录、退出、获取当前用户
- **商品模块**：发布（含图片上传）、列表、详情、下架
- **订单模块**：创建订单（原子扣减库存）

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
images: file1.jpg, file2.jpg
```

### 商品列表
```http
GET /api/product/list?page=1&size=10
```

### 创建订单
```http
POST /api/order/create
Content-Type: application/json
Cookie: JSESSIONID=xxx

{
  "productId": 1,
  "quantity": 1
}
```

## 目录结构

```
src/main/java/com/example/trade/
├── TradeApplication.java        # 启动类
├── config/                      # 配置类（拦截器、静态资源）
├── interceptor/                 # 登录拦截器
├── controller/                  # 控制器
├── service/                     # 业务逻辑
├── repository/                  # 数据访问（JdbcTemplate）
├── entity/                      # 实体类
├── dto/                         # 请求/响应DTO
├── common/                      # 统一响应封装
├── exception/                   # 全局异常处理
└── util/                        # 工具类（文件上传）
```

## 后续拓展建议

- 引入 Redis 缓存热点数据
- 使用 JWT 代替 Session 实现无状态认证
- 集成对象存储（OSS）代替本地存储
- 增加商品分类、搜索、消息通知等功能
- 完善订单状态流转与支付对接
- 添加单元测试与集成测试