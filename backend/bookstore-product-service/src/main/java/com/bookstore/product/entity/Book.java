package com.bookstore.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDateTime publishDate;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String coverImage;
    private Long categoryId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private String categoryName;
}
