package com.bookstore.order.service;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.PaymentLog;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.mapper.PaymentLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderMapper orderMapper;
    private final PaymentLogMapper paymentLogMapper;

    @Transactional
    public PaymentLog payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("订单状态不允许支付");

        // 生成模拟交易号
        String tradeNo = "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);

        // 创建支付流水
        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setOrderId(orderId);
        paymentLog.setOrderNo(order.getOrderNo());
        paymentLog.setAmount(order.getTotalAmount());
        paymentLog.setChannel("SIMULATE");
        paymentLog.setTradeNo(tradeNo);
        paymentLog.setStatus("SUCCESS");
        paymentLog.setPayTime(LocalDateTime.now());
        paymentLogMapper.insert(paymentLog);

        // 更新订单状态
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单支付成功: {} 金额: {} 交易号: {}", order.getOrderNo(), order.getTotalAmount(), tradeNo);
        return paymentLog;
    }

    public PaymentLog getByOrder(Long orderId) {
        return paymentLogMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentLog>()
                .eq(PaymentLog::getOrderId, orderId)
                .orderByDesc(PaymentLog::getCreateTime)
                .last("LIMIT 1"));
    }
}
