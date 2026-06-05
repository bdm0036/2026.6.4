-- =============================================
-- 图书封面图片更新脚本
-- 用于更新已有数据库中的图书封面
-- 图片文件位于 frontend/public/images/books/
-- =============================================

USE bookstore_product;

UPDATE tb_book SET cover_image = '/images/books/1.jpg' WHERE id = 1;
UPDATE tb_book SET cover_image = '/images/books/2.jpg' WHERE id = 2;
UPDATE tb_book SET cover_image = '/images/books/3.jpg' WHERE id = 3;
UPDATE tb_book SET cover_image = '/images/books/4.jpg' WHERE id = 4;
UPDATE tb_book SET cover_image = '/images/books/5.jpg' WHERE id = 5;
UPDATE tb_book SET cover_image = '/images/books/6.jpg' WHERE id = 6;
UPDATE tb_book SET cover_image = '/images/books/7.jpg' WHERE id = 7;
UPDATE tb_book SET cover_image = '/images/books/8.jpg' WHERE id = 8;
UPDATE tb_book SET cover_image = '/images/books/9.jpg' WHERE id = 9;
UPDATE tb_book SET cover_image = '/images/books/10.jpg' WHERE id = 10;
UPDATE tb_book SET cover_image = '/images/books/11.jpg' WHERE id = 11;
UPDATE tb_book SET cover_image = '/images/books/12.jpg' WHERE id = 12;

-- 验证更新结果
SELECT id, title, cover_image FROM tb_book ORDER BY id;
