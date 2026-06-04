# BookStore 网上书店微服务系统

## 项目概述
基于Spring Cloud微服务架构的网上书店系统，前后端分离开发，支持JWT身份认证。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2 + Spring Cloud 2023.0
- **服务注册发现**: Consul
- **API网关**: Spring Cloud Gateway
- **服务间调用**: RestTemplate + LoadBalancer
- **认证授权**: JWT (jjwt)
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0 (按服务分库)
- **缓存**: Redis 7
- **构建工具**: Maven

### 前端
- **框架**: Vue 3 + Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios

### DevOps
- **版本管理**: GitLab
- **CI/CD**: Jenkins Pipeline
- **容器化**: Docker + Docker Compose
- **编排部署**: Kubernetes

## 项目结构

```
bookstore-system/
├── backend/
│   ├── pom.xml                    # Maven父POM
│   ├── bookstore-common/          # 公共模块(实体、DTO、工具类)
│   ├── bookstore-gateway/         # API网关(8080)
│   ├── bookstore-auth-service/    # 认证服务(8081)
│   ├── bookstore-user-service/    # 用户服务(8082)
│   ├── bookstore-product-service/ # 商品服务(8083)
│   └── bookstore-order-service/   # 订单服务(8084)
├── frontend/                      # Vue 3 前端项目
├── sql/
│   └── init.sql                   # 数据库初始化脚本
├── docker/
│   ├── docker-compose.yml         # Docker Compose编排
│   └── Dockerfile                 # 后端通用Dockerfile
├── k8s/                           # Kubernetes部署文件
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── mysql.yaml
│   ├── redis.yaml
│   ├── consul.yaml
│   ├── services.yaml
│   └── ingress.yaml
└── Jenkinsfile                    # Jenkins流水线
```

## 数据库分库设计

| 数据库 | 微服务 | 读写策略 |
|--------|--------|---------|
| bookstore_user | auth-service, user-service | 读写分离，auth负责写，user负责读 |
| bookstore_product | product-service | 主库读写 + Redis缓存加速读 |
| bookstore_order | order-service | 主库读写 |

## JWT认证流程

1. 用户登录 → auth-service生成JWT Token → 通过HTTP响应体(JSON)返回 → 前端存入localStorage
2. 前端每次请求 → 通过HTTP请求头 `Authorization: Bearer <token>` 提交Token
3. Gateway JwtAuthFilter拦截验证 → 解析Token → 将用户信息写入请求头X-User-Id/X-Username/X-Role
4. 下游微服务通过UserContext获取当前用户信息

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+
- Consul (或使用Docker部署)

### 本地开发

1. 启动基础设施
```bash
cd docker
docker-compose up -d consul mysql redis
```

2. 初始化数据库
```bash
mysql -h localhost -u root -p123456 < sql/init.sql
```

3. 启动后端服务（按顺序）
```bash
cd backend
mvn clean install -DskipTests

# 启动各服务
java -jar bookstore-auth-service/target/bookstore-auth-service-1.0.0.jar
java -jar bookstore-user-service/target/bookstore-user-service-1.0.0.jar
java -jar bookstore-product-service/target/bookstore-product-service-1.0.0.jar
java -jar bookstore-order-service/target/bookstore-order-service-1.0.0.jar
java -jar bookstore-gateway/target/bookstore-gateway-1.0.0.jar
```

4. 启动前端
```bash
cd frontend
npm install
npm run dev
```

5. 访问
- 前端: http://localhost:5173
- 网关: http://localhost:8080
- Consul: http://localhost:8500

### Docker部署

```bash
cd docker
docker-compose up -d
```

### K8s部署

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/consul.yaml
kubectl apply -f k8s/services.yaml
kubectl apply -f k8s/ingress.yaml
```

## 默认账号
- 管理员: admin / 123456
- 普通用户: testuser / 123456

## API接口

### 认证服务 (8081)
- POST /api/auth/login - 用户登录
- POST /api/auth/register - 用户注册
- POST /api/auth/logout - 退出登录

### 用户服务 (8082)
- GET /api/user/profile - 获取个人信息
- PUT /api/user/profile - 更新个人信息
- GET /api/user/{id} - 获取用户信息
- GET /api/user/list - 用户列表

### 商品服务 (8083)
- GET /api/product/books - 图书列表(分页+搜索)
- GET /api/product/books/{id} - 图书详情
- GET /api/product/books/hot - 热门图书
- POST /api/product/books - 添加图书
- PUT /api/product/books/{id} - 更新图书
- DELETE /api/product/books/{id} - 下架图书
- GET /api/product/categories - 分类列表
- POST /api/product/categories - 添加分类
- PUT /api/product/categories/{id} - 更新分类
- DELETE /api/product/categories/{id} - 删除分类

### 订单服务 (8084)
- POST /api/order - 创建订单
- GET /api/order/{id} - 订单详情
- GET /api/order/my - 我的订单
- GET /api/order/all - 全部订单(管理员)
- PUT /api/order/{id}/pay - 支付订单
- PUT /api/order/{id}/cancel - 取消订单
- PUT /api/order/{id}/status - 更新订单状态
