package com.bookstore.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long bookId;
    private String bookTitle;
    private String bookCover;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
