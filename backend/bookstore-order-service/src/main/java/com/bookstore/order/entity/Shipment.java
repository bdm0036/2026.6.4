package com.bookstore.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_shipment")
public class Shipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String company;
    private String trackingNo;
    private String status;
    private LocalDateTime shipTime;
    private LocalDateTime signTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
