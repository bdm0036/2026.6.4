package com.bookstore.product.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import com.bookstore.product.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/product/books/{bookId}/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public Result<Void> rate(@PathVariable Long bookId, @RequestParam Integer score) {
        Long userId = UserContext.getUserId();
        ratingService.rate(bookId, userId, score);
        return Result.success("评分成功");
    }

    @GetMapping
    public Result<Map<String, Object>> getRating(@PathVariable Long bookId) {
        Map<String, Object> rating = ratingService.getBookRating(bookId);
        rating.put("userRating", ratingService.getUserRating(bookId, UserContext.tryGetUserId()));
        return Result.success(rating);
    }
}
