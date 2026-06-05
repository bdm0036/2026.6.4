package com.bookstore.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_rating")
public class Rating {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private Long userId;
    private Integer score;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
