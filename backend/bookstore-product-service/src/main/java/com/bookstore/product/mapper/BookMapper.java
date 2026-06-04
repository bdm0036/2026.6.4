package com.bookstore.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.product.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
