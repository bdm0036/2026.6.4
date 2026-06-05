package com.bookstore.product.controller;

import com.bookstore.common.dto.PageDTO;
import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import com.bookstore.product.entity.Review;
import com.bookstore.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/books/{bookId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Void> addReview(@PathVariable Long bookId, @RequestParam String content) {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        reviewService.addReview(bookId, userId, username != null ? username : "匿名用户", content);
        return Result.success("评论成功");
    }

    @GetMapping
    public Result<PageDTO<Review>> getReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reviewService.getReviews(bookId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long bookId, @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        reviewService.deleteReview(id, userId);
        return Result.success("删除成功");
    }
}
