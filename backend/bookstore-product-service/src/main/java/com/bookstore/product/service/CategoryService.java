package com.bookstore.product.service;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.entity.Category;
import com.bookstore.product.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_CACHE_KEY = "category:all";

    public CategoryService(CategoryMapper categoryMapper,
                           @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }

    private boolean hasRedis() { return redisTemplate != null; }

    public List<Category> listAll() {
        if (hasRedis()) {
            Object cached = redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY);
            if (cached != null) return (List<Category>) cached;
        }

        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder));

        if (hasRedis())
            redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY, categories, 2, TimeUnit.HOURS);
        return categories;
    }

    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) throw new BusinessException(404, "分类不存在");
        return category;
    }

    public void addCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        clearCache();
        log.info("新增分类: {}", category.getName());
    }

    public void updateCategory(Long id, Category category) {
        if (categoryMapper.selectById(id) == null) throw new BusinessException(404, "分类不存在");
        category.setId(id);
        categoryMapper.updateById(category);
        clearCache();
        log.info("更新分类: {}", id);
    }

    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) throw new BusinessException(404, "分类不存在");
        category.setStatus(0);
        categoryMapper.updateById(category);
        clearCache();
        log.info("删除分类: {}", id);
    }

    private void clearCache() {
        if (hasRedis()) redisTemplate.delete(CATEGORY_CACHE_KEY);
    }
}
