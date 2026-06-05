package com.bookstore.product.service;

import com.bookstore.common.dto.PageDTO;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.entity.Book;
import com.bookstore.product.entity.Category;
import com.bookstore.product.mapper.BookMapper;
import com.bookstore.product.mapper.CategoryMapper;
import com.bookstore.product.mapper.RatingMapper;
import com.bookstore.product.mapper.FavoriteMapper;
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
    private final RatingMapper ratingMapper;
    private final FavoriteMapper favoriteMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String BOOK_CACHE_KEY = "book:";
    private static final String BOOK_LIST_CACHE_KEY = "book:list:";
    private static final String HOT_BOOKS_CACHE_KEY = "book:hot";
    private static final String NULL_VALUE = "__NULL__"; // 防缓存穿透

    public Book getById(Long id) {
        String cacheKey = BOOK_CACHE_KEY + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_VALUE.equals(cached)) {
                throw new BusinessException(404, "图书不存在");
            }
            log.debug("从缓存获取图书: {}", id);
            return objectMapper.convertValue(cached, Book.class);
        }

        Book book = bookMapper.selectById(id);
        if (book == null) {
            // 防缓存穿透：缓存空值，短期过期
            redisTemplate.opsForValue().set(cacheKey, NULL_VALUE, 60, TimeUnit.SECONDS);
            throw new BusinessException(404, "图书不存在");
        }

        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                book.setCategoryName(category.getName());
            }
        }

        enrichBook(book);
        redisTemplate.opsForValue().set(cacheKey, book, 1, TimeUnit.HOURS);
        return book;
    }

    public PageDTO<Book> listBooks(Integer page, Integer size, String keyword, Long categoryId) {
        // 无搜索条件时使用缓存
        boolean canCache = !StringUtils.hasText(keyword) && (categoryId == null || categoryId <= 0);
        if (canCache) {
            String cacheKey = BOOK_LIST_CACHE_KEY + page + ":" + size;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取图书列表: page={}, size={}", page, size);
                return objectMapper.convertValue(cached, PageDTO.class);
            }
        }

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

        result.getRecords().forEach(book -> {
            if (book.getCategoryId() != null) {
                Category category = categoryMapper.selectById(book.getCategoryId());
                if (category != null) {
                    book.setCategoryName(category.getName());
                }
            }
            enrichBook(book);
        });

        PageDTO<Book> dto = new PageDTO<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());

        // 缓存图书列表（5分钟过期，热点数据自动刷新）
        if (canCache) {
            String cacheKey = BOOK_LIST_CACHE_KEY + page + ":" + size;
            redisTemplate.opsForValue().set(cacheKey, dto, 5, TimeUnit.MINUTES);
        }

        return dto;
    }

    public List<Book> getHotBooks(int limit) {
        // 热门图书缓存30分钟
        String cacheKey = HOT_BOOKS_CACHE_KEY + ":" + limit;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取热门图书");
            return objectMapper.convertValue(cached, List.class);
        }

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1)
                .orderByDesc(Book::getCreateTime)
                .last("LIMIT " + limit);
        List<Book> books = bookMapper.selectList(wrapper);

        redisTemplate.opsForValue().set(cacheKey, books, 30, TimeUnit.MINUTES);
        return books;
    }

    public void addBook(Book book) {
        book.setStatus(1);
        bookMapper.insert(book);
        clearListCache();
        log.info("新增图书: {}", book.getTitle());
    }

    public void updateBook(Long id, Book book) {
        Book existing = bookMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "图书不存在");
        }
        book.setId(id);
        bookMapper.updateById(book);
        // 清除相关缓存
        redisTemplate.delete(BOOK_CACHE_KEY + id);
        clearListCache();
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
        clearListCache();
        log.info("下架图书: {}", id);
    }

    /** 清除所有列表和热门缓存（写操作后调用） */
    private void clearListCache() {
        redisTemplate.delete(redisTemplate.keys(BOOK_LIST_CACHE_KEY + "*"));
        redisTemplate.delete(redisTemplate.keys(HOT_BOOKS_CACHE_KEY + "*"));
        log.debug("已清除图书列表和热门缓存");
    }

    private void enrichBook(Book book) {
        if (book == null) return;
        Double avgRating = ratingMapper.getAvgRating(book.getId());
        book.setAvgRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
        book.setRatingCount(ratingMapper.getRatingCount(book.getId()));
        book.setFavoriteCount(favoriteMapper.getFavoriteCount(book.getId()));
        Long userId = com.bookstore.common.utils.UserContext.tryGetUserId();
        if (userId != null) {
            Integer userRating = ratingMapper.getUserRating(book.getId(), userId);
            book.setUserRating(userRating);
            book.setFavorited(favoriteMapper.isFavorited(book.getId(), userId) > 0);
        }
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
