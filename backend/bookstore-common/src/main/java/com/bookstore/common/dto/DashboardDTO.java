package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 仪表盘统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private Long totalBooks;
    private Long totalOrders;
    private Long totalUsers;
    private BigDecimal totalRevenue;
}
