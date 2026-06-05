package com.bookstore.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.product.entity.Rating;
import com.bookstore.product.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingMapper ratingMapper;

    public void rate(Long bookId, Long userId, Integer score) {
        if (score < 1 || score > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }
        LambdaQueryWrapper<Rating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Rating::getBookId, bookId).eq(Rating::getUserId, userId);
        Rating existing = ratingMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setScore(score);
            ratingMapper.updateById(existing);
        } else {
            Rating rating = new Rating();
            rating.setBookId(bookId);
            rating.setUserId(userId);
            rating.setScore(score);
            ratingMapper.insert(rating);
        }
        log.info("用户{}对图书{}评分: {}", userId, bookId, score);
    }

    public Map<String, Object> getBookRating(Long bookId) {
        Map<String, Object> result = new HashMap<>();
        result.put("avgRating", ratingMapper.getAvgRating(bookId));
        result.put("ratingCount", ratingMapper.getRatingCount(bookId));
        return result;
    }

    public Integer getUserRating(Long bookId, Long userId) {
        if (userId == null) return null;
        return ratingMapper.getUserRating(bookId, userId);
    }

    public Map<Long, Map<String, Object>> getBatchRatings(List<Long> bookIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Long bookId : bookIds) {
            result.put(bookId, getBookRating(bookId));
        }
        return result;
    }
}
