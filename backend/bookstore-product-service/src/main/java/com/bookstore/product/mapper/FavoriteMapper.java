package com.bookstore.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.product.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT COUNT(*) FROM tb_favorite WHERE book_id = #{bookId} AND user_id = #{userId}")
    Integer isFavorited(Long bookId, Long userId);

    @Select("SELECT COUNT(*) FROM tb_favorite WHERE book_id = #{bookId}")
    Integer getFavoriteCount(Long bookId);
}
