package com.bookstore.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.product.entity.Book;
import com.bookstore.product.entity.Favorite;
import com.bookstore.product.mapper.BookMapper;
import com.bookstore.product.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;

    public void toggleFavorite(Long bookId, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getBookId, bookId).eq(Favorite::getUserId, userId);
        Favorite existing = favoriteMapper.selectOne(wrapper);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            log.info("用户{}取消收藏图书{}", userId, bookId);
        } else {
            Favorite fav = new Favorite();
            fav.setBookId(bookId);
            fav.setUserId(userId);
            favoriteMapper.insert(fav);
            log.info("用户{}收藏图书{}", userId, bookId);
        }
    }

    public boolean isFavorited(Long bookId, Long userId) {
        if (userId == null) return false;
        return favoriteMapper.isFavorited(bookId, userId) > 0;
    }

    public int getFavoriteCount(Long bookId) {
        return favoriteMapper.getFavoriteCount(bookId);
    }

    public List<Book> getMyFavorites(Long userId, Integer page, Integer size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Favorite> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Favorite> result =
                favoriteMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream()
                .map(fav -> bookMapper.selectById(fav.getBookId()))
                .collect(Collectors.toList());
    }
}
