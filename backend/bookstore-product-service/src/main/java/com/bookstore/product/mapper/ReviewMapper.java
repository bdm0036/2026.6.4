package com.bookstore.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.product.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
