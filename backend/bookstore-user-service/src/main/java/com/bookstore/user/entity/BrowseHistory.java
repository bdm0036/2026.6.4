package com.bookstore.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_browse_history")
public class BrowseHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime browseTime;
}
