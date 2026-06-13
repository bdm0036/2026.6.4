# BookStore 网上书店微服务系统

> 基于 Spring Cloud 微服务架构 + Vue 3 前后端分离的网上书店系统。毕业设计项目。

---

## 项目概述

本系统将网上书店业务拆分为**5 个独立微服务**，通过 Consul 实现服务注册发现、Gateway 实现统一入口和 JWT 认证、Docker Compose 实现一键部署。包含完整的前台购物链路和后台管理功能。

## 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2 | 基础框架 |
| Spring Cloud Gateway | 2023.0 | API 网关、路由转发、JWT 认证 |
| Spring Cloud Consul | 2023.0 | 服务注册与发现 |
| RestTemplate + LoadBalancer | — | 服务间 HTTP 调用 |
| MyBatis-Plus | 3.5 | ORM、Lambda 查询、分页 |
| jjwt | 0.12 | JWT 签发与验证（HMAC-SHA512） |
| MySQL | 8.0 | 关系型数据库（按业务分 3 库） |
| Redis | 7 | 缓存（Cache-Aside 模式） |

### 前端
| 技术 | 用途 |
|------|------|
| Vue 3 + Vite | 框架与构建 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由 + 守卫 |
| Axios | HTTP 客户端 + JWT 拦截器 |

### DevOps
| 技术 | 用途 |
|------|------|
| Docker | 容器化（多阶段构建） |
| Docker Compose | 9 容器一键编排 |
| Kubernetes | 生产级部署清单 |
| GitLab | 源码管理 |
| Jenkins | CI/CD 流水线 |

---

## 项目结构

```
bookstore-system/
├── backend/
│   ├── pom.xml                          # Maven 父 POM
│   ├── bookstore-common/                # 公共模块（实体、DTO、Result、JwtUtil、UserContext）
│   ├── bookstore-gateway/               # API 网关 :8080
│   ├── bookstore-auth-service/          # 认证服务 :8081
│   ├── bookstore-user-service/          # 用户服务 :8082
│   ├── bookstore-product-service/       # 商品服务 :8083
│   └── bookstore-order-service/         # 订单服务 :8084
├── frontend/                            # Vue 3 前端
│   └── src/
│       ├── api/index.js                 # 全部 API 封装
│       ├── router/index.js              # 路由 + 守卫
│       ├── stores/                      # Pinia 状态（cart、user）
│       ├── views/                       # 页面组件
│       │   ├── Home.vue                 # 首页
│       │   ├── Books.vue                # 图书列表
│       │   ├── BookDetail.vue           # 图书详情
│       │   ├── Cart.vue                 # 购物车
│       │   ├── Orders.vue               # 我的订单
│       │   ├── OrderDetail.vue          # 订单详情
│       │   ├── Favorites.vue            # 我的收藏
│       │   ├── CouponCenter.vue         # 优惠券中心
│       │   ├── Profile.vue              # 个人中心（地址+历史）
│       │   ├── Login.vue / Register.vue # 认证页面
│       │   └── admin/                   # 管理后台
│       │       ├── Dashboard.vue        # 仪表盘
│       │       ├── BooksManager.vue     # 图书管理
│       │       ├── CategoriesManager.vue# 分类管理
│       │       ├── OrdersManager.vue    # 订单管理
│       │       └── UsersManager.vue     # 用户管理
│       ├── components/                  # 可复用组件
│       └── assets/style.css             # 全局样式 + CSS 变量
├── sql/init.sql                         # 数据库初始化（13 张表）
├── docker/
│   ├── docker-compose.yml               # 9 容器编排
│   └── Dockerfile                       # 后端多阶段构建
├── k8s/                                 # Kubernetes 部署清单
├── docs/
│   └── 答辩报告-技术实现详解.md           # 答辩技术详解
├── 部署指南.md                           # 小白版部署教程
├── 开发报告.md                           # 完整开发报告
└── Jenkinsfile                          # CI/CD 流水线
```

---

## 微服务架构

```
浏览器 → :80 (Vue前端/Nginx) → :8080 (Gateway) → Consul 服务发现
                                                      │
                    ┌─────────────────┬───────────────┼───────────────┬─────────────────┐
                    ▼                 ▼               ▼               ▼                 ▼
              auth-service    user-service    product-service  order-service    (coupon/pay
                :8081            :8082            :8083            :8084        in order-svc)
                 │                 │                │                │
                 ▼                 ▼                ▼                ▼
          bookstore_user    bookstore_user   bookstore_product  bookstore_order
              (MySQL)          (MySQL)          (MySQL)           (MySQL)
                                                                    │
                                                              Redis :6379
```

---

## 数据库分库

| 数据库 | 包含表 | 所属服务 |
|--------|--------|---------|
| `bookstore_user` | tb_user, tb_address, tb_browse_history | auth-service, user-service |
| `bookstore_product` | tb_book, tb_category, tb_rating, tb_review, tb_favorite | product-service |
| `bookstore_order` | tb_order, tb_order_item, tb_coupon, tb_user_coupon, tb_payment_log, tb_shipment | order-service |

> 三库之间没有外键约束——微服务分库的必然选择。跨库数据一致性由应用层保证。

---

## JWT 认证流程

```
[前端]                         [Gateway]                    [AuthService]
  │                               │                              │
  │ 1. POST /api/auth/login      │                              │
  │──────────────────────────────→│ 路由到 auth-service          │
  │                               │─────────────────────────────→│
  │                               │                              │ 2. 校验密码
  │                               │                              │ 3. 签发 JWT
  │                               │ 4. 返回 Token               │
  │                               │←─────────────────────────────│
  │ 5. 收到 Token                 │                              │
  │←──────────────────────────────│                              │
  │                               │                              │
  │ 6. 存 localStorage            │                              │
  │ 7. 所有后续请求携带:          │                              │
  │    Authorization: Bearer xxx  │                              │
  │──────────────────────────────→│ 8. JwtAuthFilter 验证       │
  │                               │ 9. 注入 X-User-* 头         │
  │                               │─────────────────────────────→│ 下游直接用
```

---

## API 接口全览

### 认证服务
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| POST | `/api/auth/login` | 用户登录 | ❌ |
| POST | `/api/auth/register` | 用户注册 | ❌ |
| POST | `/api/auth/logout` | 退出登录 | ✅ |

### 用户服务
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| GET | `/api/user/profile` | 获取个人信息 | ✅ |
| PUT | `/api/user/profile` | 更新个人信息 | ✅ |
| GET | `/api/user/list` | 用户列表（管理员） | ✅ |
| PUT | `/api/user/{id}/status` | 禁用/启用用户 | ✅ |
| GET | `/api/user/addresses` | 收货地址列表 | ✅ |
| POST | `/api/user/addresses` | 新增地址 | ✅ |
| PUT | `/api/user/addresses/{id}` | 更新地址 | ✅ |
| DELETE | `/api/user/addresses/{id}` | 删除地址 | ✅ |
| GET | `/api/user/history` | 浏览历史 | ✅ |
| POST | `/api/user/history/{bookId}` | 记录浏览 | ✅ |
| DELETE | `/api/user/history` | 清除历史 | ✅ |

### 商品服务
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| GET | `/api/product/books` | 图书列表（分页+搜索） | ❌ |
| GET | `/api/product/books/{id}` | 图书详情 | ❌ |
| GET | `/api/product/books/hot` | 热门图书 | ❌ |
| GET | `/api/product/books/statistics` | 图书统计 | ✅ |
| POST | `/api/product/books` | 新增图书 | ✅ |
| PUT | `/api/product/books/{id}` | 更新图书 | ✅ |
| DELETE | `/api/product/books/{id}` | 下架图书 | ✅ |
| GET | `/api/product/categories` | 分类列表 | ❌ |
| POST | `/api/product/categories` | 新增分类 | ✅ |
| PUT | `/api/product/categories/{id}` | 更新分类 | ✅ |
| DELETE | `/api/product/categories/{id}` | 删除分类 | ✅ |
| GET | `/api/product/books/{id}/rating` | 获取评分 | ❌ |
| POST | `/api/product/books/{id}/rating` | 提交评分 | ✅ |
| GET | `/api/product/books/{id}/reviews` | 评论列表 | ❌ |
| POST | `/api/product/books/{id}/reviews` | 发表评论 | ✅ |
| DELETE | `/api/product/books/{id}/reviews/{reviewId}` | 删除评论 | ✅ |
| POST | `/api/product/books/{id}/favorite` | 切换收藏 | ✅ |
| GET | `/api/product/books/{id}/favorite` | 检查收藏状态 | ✅ |
| GET | `/api/product/favorites` | 收藏列表 | ✅ |

### 订单服务
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| POST | `/api/order` | 创建订单 | ✅ |
| GET | `/api/order/{id}` | 订单详情 | ✅ |
| GET | `/api/order/my` | 我的订单 | ✅ |
| GET | `/api/order/all` | 全部订单（管理员） | ✅ |
| GET | `/api/order/statistics` | 订单统计 | ✅ |
| PUT | `/api/order/{id}/cancel` | 取消订单 | ✅ |
| PUT | `/api/order/{id}/status` | 更新状态 | ✅ |

### 优惠券（在订单服务中）
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| GET | `/api/coupon/available` | 可领取优惠券 | ✅ |
| GET | `/api/coupon/my` | 我的优惠券 | ✅ |
| POST | `/api/coupon/{id}/claim` | 领取优惠券 | ✅ |

### 支付（在订单服务中）
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| POST | `/api/pay/{orderId}` | 模拟支付 | ✅ |
| GET | `/api/pay/order/{orderId}` | 查询支付状态 | ✅ |

### 物流（在订单服务中）
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|---------|
| POST | `/api/order/{id}/ship` | 模拟发货 | ✅ |
| GET | `/api/order/{id}/shipment` | 查询物流 | ✅ |
| GET | `/api/order/{id}/tracking` | 物流轨迹 | ✅ |

---

## 快速开始

详见 **[部署指南.md](./部署指南.md)**（小白版，每一步都有截图说明）。

最简单的方式（前提：已安装 Docker Desktop）：

```bash
cd docker
docker compose up -d
```

然后访问：
- **前端页面**：http://localhost
- **Consul 控制台**：http://localhost:8500
- **API 网关**：http://localhost:8080

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| testuser | 123456 | 普通用户 |

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [部署指南.md](./部署指南.md) | 小白版部署教程（零基础也能看懂） |
| [开发报告.md](./开发报告.md) | 完整开发与运维报告 |
| [docs/答辩报告-技术实现详解.md](./docs/答辩报告-技术实现详解.md) | 答辩用技术实现详解（含代码） |
