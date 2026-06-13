-- =============================================
-- BookStore 全栈升级 - 数据库增量脚本
-- 新增8张表，扩展已有表字段
-- =============================================

-- =============================================
-- 用户数据库扩展 (bookstore_user)
-- =============================================
USE bookstore_user;

-- 收货地址表
CREATE TABLE IF NOT EXISTS tb_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) DEFAULT NULL COMMENT '省份',
    city VARCHAR(50) DEFAULT NULL COMMENT '城市',
    district VARCHAR(50) DEFAULT NULL COMMENT '区/县',
    detail VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- 浏览历史表
CREATE TABLE IF NOT EXISTS tb_browse_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    INDEX idx_user_time (user_id, browse_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

-- 为 tb_user 增加 is_seed 字段（忽略已存在的错误）
-- MySQL 5.7 doesn't support ADD COLUMN IF NOT EXISTS, use procedure
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_col()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'bookstore_user' AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'is_seed') THEN
        ALTER TABLE tb_user ADD COLUMN is_seed TINYINT DEFAULT 0 COMMENT '是否为种子数据';
    END IF;
END //
DELIMITER ;
CALL add_col();
DROP PROCEDURE IF EXISTS add_col;

-- =============================================
-- 商品数据库扩展 (bookstore_product)
-- =============================================
USE bookstore_product;

-- 图书表增加字段
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_book_cols()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'bookstore_product' AND TABLE_NAME = 'tb_book' AND COLUMN_NAME = 'tags') THEN
        ALTER TABLE tb_book ADD COLUMN tags VARCHAR(200) DEFAULT NULL COMMENT '标签，逗号分隔';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'bookstore_product' AND TABLE_NAME = 'tb_book' AND COLUMN_NAME = 'sales') THEN
        ALTER TABLE tb_book ADD COLUMN sales INT DEFAULT 0 COMMENT '累计销量';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'bookstore_product' AND TABLE_NAME = 'tb_book' AND COLUMN_NAME = 'is_seed') THEN
        ALTER TABLE tb_book ADD COLUMN is_seed TINYINT DEFAULT 0 COMMENT '是否为种子数据';
    END IF;
END //
DELIMITER ;
CALL add_book_cols();
DROP PROCEDURE IF EXISTS add_book_cols;

-- 秒杀场次表
CREATE TABLE IF NOT EXISTS tb_seckill_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '场次名称',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status VARCHAR(20) DEFAULT 'WAITING' COMMENT 'WAITING/ACTIVE/ENDED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀场次表';

-- 秒杀商品表
CREATE TABLE IF NOT EXISTS tb_seckill_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '场次ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    seckill_stock INT NOT NULL COMMENT '秒杀库存',
    sold INT DEFAULT 0 COMMENT '已秒杀数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_book (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS tb_stock_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '图书ID',
    change_type VARCHAR(30) NOT NULL COMMENT 'ORDER_LOCK/ORDER_RELEASE/PAY_CONFIRM/SECKILL/MANUAL',
    quantity INT NOT NULL COMMENT '变更数量',
    after_stock INT NOT NULL COMMENT '变更后库存',
    order_no VARCHAR(32) DEFAULT NULL COMMENT '关联订单号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_book (book_id),
    INDEX idx_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- =============================================
-- 订单数据库扩展 (bookstore_order)
-- =============================================
USE bookstore_order;

-- 支付流水表
CREATE TABLE IF NOT EXISTS tb_payment_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    channel VARCHAR(30) DEFAULT 'SIMULATE' COMMENT '支付渠道 SIMULATE/ALIPAY/WECHAT',
    trade_no VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';

-- 物流表
CREATE TABLE IF NOT EXISTS tb_shipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    company VARCHAR(50) NOT NULL COMMENT '物流公司',
    tracking_no VARCHAR(50) NOT NULL COMMENT '物流单号',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/PICKED/TRANSIT/DELIVERING/SIGNED',
    ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
    sign_time DATETIME DEFAULT NULL COMMENT '签收时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_tracking (tracking_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流发货表';

-- 优惠券模板表
CREATE TABLE IF NOT EXISTS tb_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type VARCHAR(30) NOT NULL COMMENT 'FULL_REDUCTION/DISCOUNT/FREE_SHIPPING',
    threshold DECIMAL(10,2) DEFAULT 0 COMMENT '使用门槛',
    discount DECIMAL(10,2) NOT NULL COMMENT '减免金额/折扣率',
    total INT DEFAULT 0 COMMENT '发行总量',
    claimed INT DEFAULT 0 COMMENT '已领取数',
    valid_days INT DEFAULT 30 COMMENT '有效天数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS tb_user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status VARCHAR(20) DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
    claim_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    use_time DATETIME DEFAULT NULL COMMENT '使用时间',
    order_id BIGINT DEFAULT NULL COMMENT '使用订单ID',
    INDEX idx_user (user_id),
    INDEX idx_coupon (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 为 tb_order 增加字段
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_order_cols()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'bookstore_order' AND TABLE_NAME = 'tb_order' AND COLUMN_NAME = 'coupon_id') THEN
        ALTER TABLE tb_order ADD COLUMN coupon_id BIGINT DEFAULT NULL COMMENT '使用的优惠券ID';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'bookstore_order' AND TABLE_NAME = 'tb_order' AND COLUMN_NAME = 'discount_amount') THEN
        ALTER TABLE tb_order ADD COLUMN discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额';
    END IF;
END //
DELIMITER ;
CALL add_order_cols();
DROP PROCEDURE IF EXISTS add_order_cols;
