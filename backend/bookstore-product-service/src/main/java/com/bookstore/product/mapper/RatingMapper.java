package com.bookstore.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.product.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

    @Select("SELECT COALESCE(AVG(score), 0) FROM tb_rating WHERE book_id = #{bookId}")
    Double getAvgRating(Long bookId);

    @Select("SELECT COUNT(*) FROM tb_rating WHERE book_id = #{bookId}")
    Integer getRatingCount(Long bookId);

    @Select("SELECT score FROM tb_rating WHERE book_id = #{bookId} AND user_id = #{userId}")
    Integer getUserRating(Long bookId, Long userId);
}
