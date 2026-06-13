package com.bookstore.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private BigDecimal threshold;
    private BigDecimal discount;
    private Integer total;
    private Integer claimed;
    private Integer validDays;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
