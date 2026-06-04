package com.bookstore.order.controller;

import com.bookstore.common.dto.PageDTO;
import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import com.bookstore.order.dto.CreateOrderDTO;
import com.bookstore.order.entity.Order;
import com.bookstore.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        Order order = orderService.createOrder(createOrderDTO);
        return Result.success("下单成功", order);
    }

    @GetMapping("/{id}")
    public Result<com.bookstore.common.entity.Order> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @GetMapping("/my")
    public Result<PageDTO<Order>> listMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.listUserOrders(page, size));
    }

    @GetMapping("/all")
    public Result<PageDTO<Order>> listAllOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.listAllOrders(page, size));
    }

    @PutMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success("支付成功");
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("订单已取消");
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return Result.success("订单状态更新成功");
    }

    @GetMapping("/statistics")
    public Result<java.util.Map<String, Object>> getStatistics() {
        return Result.success(orderService.getStatistics());
    }
}
