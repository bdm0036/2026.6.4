package com.bookstore.product.service;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.entity.Category;
import com.bookstore.product.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_CACHE_KEY = "category:all";

    public List<Category> listAll() {
        // 从缓存获取
        Object cached = redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY);
        if (cached != null) {
            log.debug("从缓存获取分类列表");
            return (List<Category>) cached;
        }

        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder)
        );

        // 缓存2小时
        redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY, categories, 2, TimeUnit.HOURS);
        return categories;
    }

    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    public void addCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        redisTemplate.delete(CATEGORY_CACHE_KEY);
        log.info("新增分类: {}", category.getName());
    }

    public void updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }
        category.setId(id);
        categoryMapper.updateById(category);
        redisTemplate.delete(CATEGORY_CACHE_KEY);
        log.info("更新分类: {}", id);
    }

    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        category.setStatus(0);
        categoryMapper.updateById(category);
        redisTemplate.delete(CATEGORY_CACHE_KEY);
        log.info("删除分类: {}", id);
    }
}
