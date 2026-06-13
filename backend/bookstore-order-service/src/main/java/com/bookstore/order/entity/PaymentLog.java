package com.bookstore.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_payment_log")
public class PaymentLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private String channel;
    private String tradeNo;
    private String status;
    private LocalDateTime payTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
