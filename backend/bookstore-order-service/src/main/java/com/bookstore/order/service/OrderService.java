package com.bookstore.order.service;

import com.bookstore.common.dto.PageDTO;
import com.bookstore.common.entity.Result;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.utils.UserContext;
import com.bookstore.order.dto.CreateOrderDTO;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.mapper.OrderItemMapper;
import com.bookstore.order.mapper.OrderMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final RestTemplate restTemplate;

    /**
     * 调用商品服务获取图书信息（带熔断和重试保护）
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getBookInfoFallback")
    @Retry(name = "productService")
    private Map<String, Object> getBookInfo(Long bookId) {
        String url = "http://bookstore-product-service/api/product/books/" + bookId;
        ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
        );
        Result<Map<String, Object>> result = response.getBody();
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException("获取图书信息失败");
        }
        return result.getData();
    }

    /** 熔断降级：返回缓存的价格（实际可用Redis缓存） */
    private Map<String, Object> getBookInfoFallback(Long bookId, Throwable t) {
        log.error("商品服务熔断降级: bookId={}, error={}", bookId, t.getMessage());
        throw new BusinessException("商品服务暂时不可用，已触发熔断保护，请稍后重试");
    }

    @Transactional
    public Order createOrder(CreateOrderDTO createOrderDTO) {
        Long userId = UserContext.getUserId();

        // 生成订单号
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 通过RestTemplate调用商品服务获取图书信息（带熔断+重试）
        for (CreateOrderDTO.OrderItemDTO itemDTO : createOrderDTO.getItems()) {
            try {
                Map<String, Object> bookData = getBookInfo(itemDTO.getBookId());

                BigDecimal price = new BigDecimal(bookData.get("price").toString());
                String title = (String) bookData.get("title");
                String coverImage = (String) bookData.get("coverImage");
                Integer stock = (Integer) bookData.get("stock");

                if (stock < itemDTO.getQuantity()) {
                    throw new BusinessException("图书《" + title + "》库存不足，当前库存: " + stock);
                }

                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                OrderItem item = new OrderItem();
                item.setBookId(itemDTO.getBookId());
                item.setBookTitle(title);
                item.setBookCover(coverImage);
                item.setPrice(price);
                item.setQuantity(itemDTO.getQuantity());
                item.setSubtotal(subtotal);
                orderItems.add(item);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("调用商品服务失败", e);
                throw new BusinessException("商品服务暂时不可用，请稍后重试");
            }
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setReceiverName(createOrderDTO.getReceiverName());
        order.setReceiverPhone(createOrderDTO.getReceiverPhone());
        order.setReceiverAddress(createOrderDTO.getReceiverAddress());
        order.setRemark(createOrderDTO.getRemark());

        orderMapper.insert(order);

        // 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        log.info("创建订单成功: {} 金额: {} 用户: {}", orderNo, totalAmount, userId);
        return order;
    }

    public com.bookstore.common.entity.Order getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
        );

        // 转换为common entity返回
        com.bookstore.common.entity.Order detail = new com.bookstore.common.entity.Order();
        detail.setId(order.getId());
        detail.setOrderNo(order.getOrderNo());
        detail.setUserId(order.getUserId());
        detail.setTotalAmount(order.getTotalAmount());
        detail.setStatus(order.getStatus());
        detail.setReceiverName(order.getReceiverName());
        detail.setReceiverPhone(order.getReceiverPhone());
        detail.setReceiverAddress(order.getReceiverAddress());
        detail.setRemark(order.getRemark());
        detail.setPayTime(order.getPayTime());
        detail.setCreateTime(order.getCreateTime());
        detail.setUpdateTime(order.getUpdateTime());

        List<com.bookstore.common.entity.OrderItem> commonItems = items.stream().map(i -> {
            com.bookstore.common.entity.OrderItem ci = new com.bookstore.common.entity.OrderItem();
            ci.setId(i.getId());
            ci.setOrderId(i.getOrderId());
            ci.setBookId(i.getBookId());
            ci.setBookTitle(i.getBookTitle());
            ci.setBookCover(i.getBookCover());
            ci.setPrice(i.getPrice());
            ci.setQuantity(i.getQuantity());
            ci.setSubtotal(i.getSubtotal());
            return ci;
        }).toList();
        detail.setItems(commonItems);

        return detail;
    }

    public PageDTO<Order> listUserOrders(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        Page<Order> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        return new PageDTO<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());
    }

    public PageDTO<Order> listAllOrders(Integer page, Integer size) {
        Page<Order> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        return new PageDTO<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());
    }

    public void payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单 {} 支付成功", order.getOrderNo());
    }

    public void cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待支付订单");
        }
        order.setStatus(4);
        orderMapper.updateById(order);
        log.info("订单 {} 已取消", order.getOrderNo());
    }

    public void updateStatus(Long orderId, Integer status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
        log.info("订单 {} 状态更新为: {}", order.getOrderNo(), status);
    }

    public java.util.Map<String, Object> getStatistics() {
        Long totalOrders = orderMapper.selectCount(null);
        java.util.List<com.bookstore.order.entity.Order> allOrders = orderMapper.selectList(null);
        java.math.BigDecimal totalRevenue = allOrders.stream()
                .map(com.bookstore.order.entity.Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        return stats;
    }
}
