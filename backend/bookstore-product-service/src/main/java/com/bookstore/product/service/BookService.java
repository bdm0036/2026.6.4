package com.bookstore.product.service;

import com.bookstore.common.dto.PageDTO;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.entity.Book;
import com.bookstore.product.entity.Category;
import com.bookstore.product.mapper.BookMapper;
import com.bookstore.product.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String BOOK_CACHE_KEY = "book:";
    private static final String BOOK_LIST_CACHE_KEY = "book:list:page:";

    public Book getById(Long id) {
        // 先从Redis缓存获取
        String cacheKey = BOOK_CACHE_KEY + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取图书: {}", id);
            return objectMapper.convertValue(cached, Book.class);
        }

        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }

        // 关联分类名称
        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                book.setCategoryName(category.getName());
            }
        }

        // 写入缓存，1小时过期
        redisTemplate.opsForValue().set(cacheKey, book, 1, TimeUnit.HOURS);
        return book;
    }

    public PageDTO<Book> listBooks(Integer page, Integer size, String keyword, Long categoryId) {
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Book::getCreateTime);

        Page<Book> result = bookMapper.selectPage(pageParam, wrapper);

        // 关联分类名称
        result.getRecords().forEach(book -> {
            if (book.getCategoryId() != null) {
                Category category = categoryMapper.selectById(book.getCategoryId());
                if (category != null) {
                    book.setCategoryName(category.getName());
                }
            }
        });

        return new PageDTO<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());
    }

    public List<Book> getHotBooks(int limit) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1)
                .orderByDesc(Book::getCreateTime)
                .last("LIMIT " + limit);
        return bookMapper.selectList(wrapper);
    }

    public void addBook(Book book) {
        book.setStatus(1);
        bookMapper.insert(book);
        log.info("新增图书: {}", book.getTitle());
    }

    public void updateBook(Long id, Book book) {
        Book existing = bookMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "图书不存在");
        }
        book.setId(id);
        bookMapper.updateById(book);
        // 清除缓存
        redisTemplate.delete(BOOK_CACHE_KEY + id);
        log.info("更新图书: {}", id);
    }

    public void deleteBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        book.setStatus(0);
        bookMapper.updateById(book);
        redisTemplate.delete(BOOK_CACHE_KEY + id);
        log.info("下架图书: {}", id);
    }

    public java.util.Map<String, Object> getStatistics() {
        Long totalBooks = bookMapper.selectCount(new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1));
        Long totalCategories = categoryMapper.selectCount(null);
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalBooks", totalBooks);
        stats.put("totalCategories", totalCategories);
        return stats;
    }
}
