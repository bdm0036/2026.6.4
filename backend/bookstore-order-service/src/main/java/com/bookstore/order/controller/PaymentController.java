package com.bookstore.order.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.order.entity.PaymentLog;
import com.bookstore.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public Result<PaymentLog> pay(@PathVariable Long orderId) {
        PaymentLog log = paymentService.payOrder(orderId);
        return Result.success("支付成功", log);
    }

    @GetMapping("/order/{orderId}")
    public Result<PaymentLog> getPayment(@PathVariable Long orderId) {
        return Result.success(paymentService.getByOrder(orderId));
    }
}
