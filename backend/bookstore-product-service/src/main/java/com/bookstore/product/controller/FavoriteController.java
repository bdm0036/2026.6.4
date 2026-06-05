package com.bookstore.product.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import com.bookstore.product.entity.Book;
import com.bookstore.product.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/books/{bookId}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long bookId) {
        Long userId = UserContext.getUserId();
        favoriteService.toggleFavorite(bookId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favoriteService.isFavorited(bookId, userId));
        result.put("favoriteCount", favoriteService.getFavoriteCount(bookId));
        return Result.success(result);
    }

    @GetMapping("/books/{bookId}/favorite")
    public Result<Map<String, Object>> checkFavorite(@PathVariable Long bookId) {
        Long userId = UserContext.tryGetUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favoriteService.isFavorited(bookId, userId));
        result.put("favoriteCount", favoriteService.getFavoriteCount(bookId));
        return Result.success(result);
    }

    @GetMapping("/favorites")
    public Result<List<Book>> getMyFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(favoriteService.getMyFavorites(userId, page, size));
    }
}
