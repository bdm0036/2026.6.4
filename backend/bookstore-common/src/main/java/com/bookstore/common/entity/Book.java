package com.bookstore.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
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
    private String categoryName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
