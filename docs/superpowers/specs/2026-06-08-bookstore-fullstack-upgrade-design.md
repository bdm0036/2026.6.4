# BookStore 网上书店 — 全栈深度重构设计

> 日期：2026-06-08
> 类型：全栈升级（前端体验 + 后端业务 + 数据丰富）
> 基线：Spring Boot 3.2 + Spring Cloud 2023.0 + Vue 3 + Element Plus

---

## 一、概述

当前系统具备微服务骨架和基础CRUD能力，但功能较为基础。本次升级目标是将系统提升为商业级网上书店，分两个 Phase 执行。

### Phase 1：前端体验升级
- 6个现有页面深度重写
- 3个全新页面（订单详情、地址管理、搜索结果页，搜索结果合并到图书列表页）
- 全局UX组件（骨架屏、暗色模式、回到顶部、全局搜索）
- 管理后台增加图表统计

### Phase 2：后端业务强化
- 4个全新业务模块（支付、物流、优惠券、秒杀）
- 5个现有服务增强（库存扣减、文件上传、定时任务、数据统计、浏览历史）
- 对应前端集成

---

## 二、数据库增强

### 2.1 新增表

```sql
-- 收货地址
CREATE TABLE address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail VARCHAR(255) NOT NULL,
    is_default TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 优惠券模板
CREATE TABLE coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL COMMENT 'FULL_REDUCTION/DISCOUNT/FREE_SHIPPING',
    threshold DECIMAL(10,2) DEFAULT 0 COMMENT '使用门槛金额',
    discount DECIMAL(10,2) NOT NULL COMMENT '减免金额或折扣率',
    total INT DEFAULT 0 COMMENT '总发行量',
    claimed INT DEFAULT 0 COMMENT '已领取数',
    valid_days INT DEFAULT 30 COMMENT '有效天数',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户优惠券
CREATE TABLE user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
    claim_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    use_time DATETIME,
    order_id BIGINT
);

-- 秒杀场次
CREATE TABLE seckill_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING' COMMENT 'WAITING/ACTIVE/ENDED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 秒杀商品关联
CREATE TABLE seckill_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    seckill_price DECIMAL(10,2) NOT NULL,
    seckill_stock INT NOT NULL COMMENT '秒杀库存',
    sold INT DEFAULT 0 COMMENT '已秒杀数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 支付流水
CREATE TABLE payment_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    channel VARCHAR(30) COMMENT 'ALIPAY/WECHAT/SIMULATE',
    trade_no VARCHAR(64) COMMENT '第三方交易号',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    pay_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 物流发货
CREATE TABLE shipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    company VARCHAR(50) NOT NULL COMMENT '物流公司',
    tracking_no VARCHAR(50) NOT NULL COMMENT '物流单号',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/PICKED/TRANSIT/DELIVERING/SIGNED',
    ship_time DATETIME,
    sign_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 浏览历史
CREATE TABLE browse_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, browse_time DESC)
);

-- 库存流水
CREATE TABLE stock_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    change_type VARCHAR(30) NOT NULL COMMENT 'ORDER_LOCK/ORDER_RELEASE/PAY_CONFIRM/SECKILL/MANUAL',
    quantity INT NOT NULL COMMENT '变更数量（正数为增，负数为减）',
    after_stock INT NOT NULL COMMENT '变更后库存',
    order_no VARCHAR(32),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2.2 种子数据计划

**图书分类（10个）**：
文学小说、计算机科学、经济管理、哲学心理、历史人文、艺术设计、生活百科、少儿读物、教辅考试、科技科普

**图书数据（150+本）**：
- 每个分类15-20本
- 包含真实感元数据（书名、作者、ISBN、出版社、价格、简介）
- 封面使用纯色占位 + 书名首字方案，前端CSS生成
- 预置 tags 标签字段（如"经典/入门/必读"）
- 分布不同价格区间（20-200元）和库存量（5-100）

**测试用户（10个）**：
- 1个管理员：admin / 123456
- 9个普通用户：booklover1-9 / 123456
- 每个用户有随机收藏、评分、评论数据

**模拟订单（50+条）**：
- 覆盖各状态（待支付/已支付/已发货/已完成/已取消）
- 时间跨度过去30天
- 关联随机用户和图书
- 部分订单有支付流水和物流记录

### 2.3 数据初始化方案

使用 Java 启动监听器 `DataSeeder`：
- 启动时检查图书数量，< 50 本时自动填充
- 启动时检查用户数量，< 5 个时自动填充
- 登录时检查是否需要填充演示数据（订单/评论等）
- 所有填充数据标记 `is_seed = true`，可清理

封面不依赖外部URL，前端统一使用 CSS 生成彩色占位图（取书名首字 + 基于分类的固定色系渐变色）。分类色系映射：文学-暖橙、计算机-深蓝、经济-墨绿、哲学-紫、历史-棕、艺术-粉、生活-青、少儿-彩虹、教辅-灰蓝、科技-靛蓝。前端 BookCover.vue 组件统一实现。

---

## 三、Phase 1：前端页面改造

### 3.1 首页 Home（重写）

**新增组件**：
- `BannerCarousel.vue`：3张轮播Banner（大促/新书上架/专题推荐），自动播放
- `BookSection.vue`：可复用图书展示区，支持Tab切换
- `BookCard.vue`：统一图书卡片（含标签徽章、评分、价格、加购按钮）

**布局**：
```
Hero搜索区（保留，缩小高度）
   ↓
轮播Banner（新增，3张自动轮播）
   ↓
Tab图书区（热门推荐 / 新书上架 / 限时特价）
   ↓
分类浏览（图标网格，保留并增强样式）
   ↓
猜你喜欢（登录后基于浏览历史，Phase 2 数据到位后启用）
```

### 3.2 图书列表 Books（增强）

**新增功能**：
- 排序：默认/价格↑/价格↓/评分最高/销量最高/最新上架
- 价格区间滑块（el-slider，0-500元）
- 视图切换：网格视图 / 列表视图
- 图书卡片增加：标签徽章、库存状态
- URL参数同步（搜索条件可在URL中分享）

### 3.3 图书详情 BookDetail（增强）

**新增区块**：
- 图书标签展示（Tag列表）
- 销售趋势小图表（近30天销量柱状图，ECharts mini）
- 相关推荐横向滚动条（同分类图书）
- 富文本简介（支持HTML渲染）
- UI优化：评分/收藏/评论区域布局优化

### 3.4 购物车 Cart（增强）

**新增功能**：
- 库存状态标识（充足绿/紧张黄/缺货红）
- 批量选择/删除模式
- 收货地址选择（从地址库弹窗选择）
- 优惠券选择（Phase 2 后端就绪后集成）
- 价格明细：商品金额 + 运费 + 优惠减免 = 实付款

### 3.5 订单系统

**订单列表 Orders（增强）**：
- 新增状态筛选Tab（全部/待支付/已支付/已发货/已完成/已取消）
- 订单卡片增加物流进度条
- 增强订单操作（支付/取消/确认收货/查看物流/申请退款）

**订单详情 OrderDetail（全新页面）**：
- 订单状态流程指示器（Steps组件）
- 物流时间线（Timeline组件，Phase 2）
- 订单商品列表
- 价格明细
- 收货信息卡片
- 操作按钮区

### 3.6 个人中心 Profile（重构为Tab式）

| Tab | 内容 |
|-----|------|
| 基本信息 | 头像（CSS生成）、昵称、邮箱、手机号编辑 |
| 安全设置 | 修改密码（旧密码+新密码+确认） |
| 收货地址 | 地址列表 + 新增/编辑/删除 + 设默认地址 |
| 浏览历史 | 图书网格列表 + 清除历史 |

### 3.7 管理后台 Admin（增强）

**Dashboard 增强**：
- 近7天销售趋势折线图（ECharts）
- 分类销售额饼图
- 新增用户趋势
- 待处理订单数提醒

**图书管理增强**：
- 富文本简介编辑器（Quill或简单的Markdown）
- 批量导入/导出（Excel）

### 3.8 全局体验

| 特性 | 实现方式 |
|------|---------|
| 骨架屏 | el-skeleton 组件，首页/列表/详情加载态 |
| 暗色模式 | CSS变量 `--bg-primary` 等，`html.dark` 切换，localStorage持久 |
| 回到顶部 | `el-backtop` |
| 全局搜索 | 顶部导航栏放大镜按钮 → 弹出搜索面板 |
| 空状态 | 统一空状态插图组件 `EmptyState.vue` |

---

## 四、Phase 2：后端业务强化

### 4.1 支付模块

**PaymentService 核心逻辑**：
- `createPayment(orderId)` → 生成支付单，返回支付链接（沙箱环境）
- `handleCallback(params)` → 验签 + 更新订单状态 + 记录流水
- 保留现有 `/pay` 作为模拟支付降级方案

**支付宝沙箱**：
- 使用支付宝开放平台沙箱环境
- 配置 `application.yml` 中的 appId、私钥、公钥
- 回调地址：`POST /api/pay/callback/alipay`

**PaymentLog 记录**：
- 每笔支付完整生命周期：PENDING → SUCCESS/FAILED
- 关联第三方交易号
- 支持对账

### 4.2 库存扣减

**流程**：
```
1. 下单 → Redis DECR 预扣库存
2. 支付成功 → 确认扣减，写 stock_log
3. 超时30min未支付 → Redis INCR 回滚库存
4. 取消订单 → Redis INCR 回滚库存
```

**定时任务**：`@Scheduled(fixedRate = 60000)` 每分钟扫描超时订单，释放库存

**Redis Key设计**：
- `book:stock:{bookId}` — 可售库存
- `book:stock:locked:{orderNo}` — 该订单锁定的库存量

### 4.3 物流模块

**ShipmentController**：
- `POST /api/order/{id}/ship` — 商家发货（管理员）
- `GET /api/order/{id}/shipment` — 查询物流信息
- `GET /api/order/{id}/tracking` — 模拟物流轨迹

**模拟物流轨迹**：
```
已支付 → 商家拣货中 → 快递已揽收 → 运输中 → 派送中 → 已签收
```
按订单时间自动生成轨迹节点，每个节点有时间和描述。

### 4.4 优惠券模块

**CouponController**：
- `GET /api/coupon/available` — 可领取优惠券
- `POST /api/coupon/{id}/claim` — 领取
- `GET /api/coupon/my` — 我的优惠券
- `GET /api/coupon/order/{orderId}/usable` — 该订单可用的优惠券

**下单集成**：
- CreateOrderDTO 增加 `couponId` 字段
- OrderService 计算优惠后金额
- 优惠券使用后标记为 USED

**种子优惠券**：
- 满50减5、满100减15、满200减40
- 8折优惠券（最高减30）
- 免邮券

### 4.5 秒杀模块

**SeckillController**：
- `GET /api/seckill/sessions` — 秒杀场次列表
- `GET /api/seckill/books` — 某场次的秒杀商品
- `POST /api/seckill/{bookId}/execute` — 执行秒杀

**秒杀流程（Redis原子操作）**：
```
1. 用户请求秒杀
2. Redis Lua脚本原子操作：
   - 检查库存 > 0
   - 检查是否重复秒杀
   - DECR 库存
   - 记录用户已秒杀
3. 成功 → 发送下单消息（异步创建订单）
4. 失败 → 返回原因（已抢光/重复秒杀/未开始/已结束）
```

**Redis Key设计**：
- `seckill:stock:{bookId}` — 秒杀库存
- `seckill:users:{bookId}` — 已秒杀用户集合
- `seckill:order:queue` — 秒杀下单队列

**注意**：秒杀模块如果引入消息队列会增加复杂度。简化方案：秒杀成功后直接同步创建订单（事务），库存用Redis保证原子性，创建订单失败则回滚Redis库存。

### 4.6 文件上传

**MinIO 对象存储**：
- Docker Compose 增加 MinIO 容器
- `FileController`：上传/下载/预览
- 图书封面：`POST /api/file/upload/cover`
- 用户头像：`POST /api/file/upload/avatar`
- 前端：el-upload 组件集成

### 4.7 定时任务

统一在 `bookstore-order-service` 中管理（`@EnableScheduling`）：

| 任务 | 频率 | 说明 |
|------|------|------|
| 取消超时订单 | 每分钟 | 未支付超过30分钟 → 取消 + 释放库存 |
| 过期优惠券 | 每天凌晨 | 超过有效期的优惠券标记 EXPIRED |
| 更新热销排行 | 每小时 | Redis缓存热销Top20 |
| 清理过期秒杀 | 每分钟 | 已结束的秒杀场次状态更新 |

### 4.8 数据统计增强

**订单统计**：
- 今日/本周/本月销售额
- 近7天/30天销售趋势（用于ECharts折线图）
- 各状态订单数量分布

**图书统计**：
- 分类图书数量分布（用于饼图）
- 销量Top10
- 评分Top10

**用户统计**：
- 近7天新增用户数
- 用户活跃度（有订单的用户数）

### 4.9 接口限流

**Gateway层 Redis 令牌桶限流**：
- 全局限制：每IP每秒100次请求
- 登录接口：每IP每分钟5次（防暴力破解）
- 秒杀接口：每用户每场次1次

---

## 五、实施顺序

### Phase 1 实施步骤（建议顺序）
1. 数据初始化（DataSeeder + 150+图书种子数据）
2. 全局体验组件（骨架屏、暗色模式、回到顶部、空状态）
3. 首页重写（轮播Banner + Tab图书区）
4. 图书列表增强（排序、价格筛选、视图切换）
5. 图书详情增强（标签、推荐、趋势图）
6. 购物车增强（库存状态、地址选择、批量操作）
7. 订单详情页（全新）
8. 个人中心重构（Tab式 + 地址管理）
9. 管理后台增强（ECharts图表）
10. 全局搜索

### Phase 2 实施步骤（建议顺序）
1. 数据库新增表结构 + 实体类
2. 地址管理 CRUD
3. 文件上传（MinIO）
4. 库存扣减（Redis + 定时任务）
5. 支付模块（支付宝沙箱）
6. 物流模块
7. 优惠券模块
8. 浏览历史 + 猜你喜欢
9. 定时任务注册
10. 秒杀模块（最后，复杂度最高）
11. 接口限流
12. 数据统计增强

---

## 六、技术约束与注意事项

- 所有新增表使用 MyBatis-Plus 代码生成器生成基础 CRUD
- Redis 操作使用 Spring Data Redis + Lua 脚本保证原子性
- 封面采用纯色CSS占位方案，不依赖外部图片URL
- 秒杀模块在 Phase 2 最后实施，确保其他功能稳定后再引入高并发复杂度
- 接口限流在 Gateway 层统一处理，不影响业务代码
- 前端组件尽可能复用，提取公共 BookCard、EmptyState 等组件
- 数据库连接信息不硬编码，通过 application.yml 和 K8s ConfigMap 管理
