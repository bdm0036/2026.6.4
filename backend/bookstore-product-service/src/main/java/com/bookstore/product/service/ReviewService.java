package com.bookstore.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.dto.PageDTO;
import com.bookstore.product.entity.Review;
import com.bookstore.product.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    public void addReview(Long bookId, Long userId, String username, String content) {
        Review review = new Review();
        review.setBookId(bookId);
        review.setUserId(userId);
        review.setUsername(username);
        review.setContent(content);
        reviewMapper.insert(review);
        log.info("用户{}评论图书{}: {}", userId, bookId, content.substring(0, Math.min(20, content.length())));
    }

    public PageDTO<Review> getReviews(Long bookId, Integer page, Integer size) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getBookId, bookId).orderByDesc(Review::getCreateTime);
        Page<Review> result = reviewMapper.selectPage(pageParam, wrapper);
        return new PageDTO<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());
    }

    public void deleteReview(Long id, Long userId) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new com.bookstore.common.exception.BusinessException(404, "评论不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new com.bookstore.common.exception.BusinessException(403, "无权删除他人评论");
        }
        reviewMapper.deleteById(id);
        log.info("删除评论: {}", id);
    }
}
