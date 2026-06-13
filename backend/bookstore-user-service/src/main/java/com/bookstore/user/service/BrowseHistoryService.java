package com.bookstore.user.service;

import com.bookstore.user.entity.BrowseHistory;
import com.bookstore.user.mapper.BrowseHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BrowseHistoryService {

    private final BrowseHistoryMapper browseHistoryMapper;
    private final JdbcTemplate jdbc;

    public void record(Long userId, Long bookId) {
        BrowseHistory exist = browseHistoryMapper.selectOne(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getBookId, bookId));
        if (exist != null) {
            exist.setBrowseTime(LocalDateTime.now());
            browseHistoryMapper.updateById(exist);
        } else {
            BrowseHistory bh = new BrowseHistory();
            bh.setUserId(userId);
            bh.setBookId(bookId);
            bh.setBrowseTime(LocalDateTime.now());
            browseHistoryMapper.insert(bh);
        }
    }

    public List<Map<String, Object>> list(Long userId, int page, int size) {
        String sql = "SELECT b.id, b.title, b.author, b.price, b.cover_image AS coverImage, " +
                     "b.category_id AS categoryId, b.stock, '' AS tags, " +
                     "c.name AS categoryName, bh.browse_time AS browseTime " +
                     "FROM bookstore_user.tb_browse_history bh " +
                     "JOIN bookstore_product.tb_book b ON bh.book_id = b.id " +
                     "LEFT JOIN bookstore_product.tb_category c ON b.category_id = c.id " +
                     "WHERE bh.user_id = ? " +
                     "ORDER BY bh.browse_time DESC " +
                     "LIMIT ? OFFSET ?";
        int offset = (page - 1) * size;
        return jdbc.queryForList(sql, userId, size, offset);
    }

    public void clear(Long userId) {
        browseHistoryMapper.delete(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId));
    }
}
