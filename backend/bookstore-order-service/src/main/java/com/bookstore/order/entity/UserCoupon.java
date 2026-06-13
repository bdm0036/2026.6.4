package com.bookstore.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private String status;
    private LocalDateTime claimTime;
    private LocalDateTime useTime;
    private Long orderId;
}
