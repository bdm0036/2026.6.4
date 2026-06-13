package com.bookstore.order.config;

import com.bookstore.order.entity.Order;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.service.StockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final OrderMapper orderMapper;
    private final StockService stockService;

    /**
     * 每分钟扫描超时未支付订单（30分钟），自动取消并释放库存
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<Order> timeoutOrders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, timeout));

        for (Order order : timeoutOrders) {
            try {
                order.setStatus(4);
                orderMapper.updateById(order);
                stockService.releaseStock(order.getOrderNo());
                log.info("超时订单自动取消: {}", order.getOrderNo());
            } catch (Exception e) {
                log.error("取消超时订单失败: {}", order.getOrderNo(), e);
            }
        }
    }

    /**
     * 每小时更新热销排行（Redis缓存）
     */
    @Scheduled(fixedRate = 3600000)
    public void updateHotBooks() {
        log.debug("更新热销排行缓存...");
        // Phase 2.8 实现
    }
}
