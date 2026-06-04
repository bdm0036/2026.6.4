package com.bookstore.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "收货电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    private String remark;

    @Data
    public static class OrderItemDTO {
        private Long bookId;
        private Integer quantity;
    }
}
