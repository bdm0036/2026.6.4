-- =============================================
-- 网上书店系统 - 数据库初始化脚本
-- 按微服务分库设计：每个服务独立数据库
-- =============================================

SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bookstore_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bookstore_product DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bookstore_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =============================================
-- 用户数据库 (bookstore_user) - 用于auth-service和user-service
-- =============================================
USE bookstore_user;

DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试用户（密码为123456的MD5值：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO tb_user (username, password, email, phone, nickname, status, role) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@bookstore.com', '13800000000', '系统管理员', 1, 'ADMIN'),
('testuser', 'e10adc3949ba59abbe56e057f20f883e', 'test@bookstore.com', '13900000001', '测试用户', 1, 'USER');

-- =============================================
-- 商品数据库 (bookstore_product) - 用于product-service
-- =============================================
USE bookstore_product;

DROP TABLE IF EXISTS tb_category;
CREATE TABLE tb_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

DROP TABLE IF EXISTS tb_book;
CREATE TABLE tb_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) DEFAULT NULL COMMENT '作者',
    isbn VARCHAR(20) DEFAULT NULL COMMENT 'ISBN号',
    publisher VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    publish_date DATE DEFAULT NULL COMMENT '出版日期',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    description TEXT COMMENT '图书简介',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category_id),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

DROP TABLE IF EXISTS tb_rating;
CREATE TABLE tb_rating (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '图书ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    score TINYINT NOT NULL COMMENT '评分 1-5',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_book_user (book_id, user_id),
    INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评分表';

DROP TABLE IF EXISTS tb_review;
CREATE TABLE tb_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '图书ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    content TEXT NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_book_id (book_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评论表';

DROP TABLE IF EXISTS tb_favorite;
CREATE TABLE tb_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '图书ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_book_user (book_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 插入分类数据
INSERT INTO tb_category (name, description, sort_order) VALUES
('文学小说', '古今中外文学名著、现代小说', 1),
('计算机科学', '编程语言、算法、人工智能等技术书籍', 2),
('人文社科', '历史、哲学、心理学、社会学', 3),
('经济管理', '经济学、管理学、投资理财', 4),
('科学技术', '物理、数学、生物等自然科学', 5),
('生活教育', '生活百科、教育教材、考试辅导', 6);

-- 插入图书数据
INSERT INTO tb_book (title, author, isbn, publisher, publish_date, price, stock, description, category_id, status) VALUES
('深入理解Java虚拟机', '周志明', '978-7-111-63423-6', '机械工业出版社', '2019-12-01', 129.00, 50, '全面讲解JVM工作原理、内存管理、类加载机制和编译优化', 2, 1),
('Spring微服务实战', 'John Carnell', '978-7-115-48297-5', '人民邮电出版社', '2018-06-01', 89.00, 30, 'Spring Boot和Spring Cloud构建微服务系统的实践指南', 2, 1),
('百年孤独', '加西亚·马尔克斯', '978-7-5442-5399-4', '南海出版公司', '2011-06-01', 55.00, 100, '魔幻现实主义文学的代表作，讲述布恩迪亚家族七代人的传奇故事', 1, 1),
('活着', '余华', '978-7-5063-6543-7', '作家出版社', '2012-08-01', 28.00, 80, '讲述了一个人一生的故事，展现了中国近代历史的变迁', 1, 1),
('算法导论', 'Thomas H.Cormen', '978-7-111-40701-0', '机械工业出版社', '2013-01-01', 128.00, 25, '计算机算法的经典教材，全面介绍各种算法及其分析', 2, 1),
('人类简史', '尤瓦尔·赫拉利', '978-7-5086-4735-7', '中信出版社', '2014-11-01', 68.00, 60, '从十万年前有生命迹象开始到21世纪的人类发展史', 3, 1),
('经济学原理', '曼昆', '978-7-301-25069-0', '北京大学出版社', '2015-01-01', 98.00, 40, '全球最受欢迎的经济学入门教材', 4, 1),
('三体', '刘慈欣', '978-7-5366-9293-0', '重庆出版社', '2008-01-01', 39.00, 120, '中国科幻文学的里程碑之作，讲述地球文明与三体文明的接触', 1, 1),
('设计模式', 'GoF', '978-7-111-07575-0', '机械工业出版社', '2004-09-01', 69.00, 20, '面向对象设计的经典之作，23种设计模式的权威指南', 2, 1),
('思考，快与慢', '丹尼尔·卡尼曼', '978-7-5086-3355-8', '中信出版社', '2012-07-01', 69.00, 45, '诺贝尔经济学奖得主关于人类认知与决策的研究', 3, 1),
('时间简史', '史蒂芬·霍金', '978-7-5357-4738-6', '湖南科技出版社', '2010-04-01', 45.00, 35, '霍金关于宇宙学的经典科普之作', 5, 1),
('Java并发编程艺术', '方腾飞', '978-7-111-50824-3', '机械工业出版社', '2015-07-01', 79.00, 30, '深入剖析Java并发编程的底层实现原理', 2, 1);

-- =============================================
-- 订单数据库 (bookstore_order) - 用于order-service
-- =============================================
USE bookstore_order;

DROP TABLE IF EXISTS tb_order;
CREATE TABLE tb_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货电话',
    receiver_address VARCHAR(200) NOT NULL COMMENT '收货地址',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

DROP TABLE IF EXISTS tb_order_item;
CREATE TABLE tb_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    book_title VARCHAR(200) NOT NULL COMMENT '图书名称',
    book_cover VARCHAR(500) DEFAULT NULL COMMENT '图书封面',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    INDEX idx_order_id (order_id),
    INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';
