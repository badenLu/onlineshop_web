package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    private String remark;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "SKU ID is required")
        private Long skuId;

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}