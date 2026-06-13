package com.bookstore.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.user.entity.BrowseHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BrowseHistoryMapper extends BaseMapper<BrowseHistory> {
}
