package com.bookstore.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
