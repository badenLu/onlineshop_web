package com.ecommerce.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CartItemResponse {
    private Long skuId;
    private Long productId;
    private String productName;
    private String productImage;
    private String skuCode;
    private Map<String, String> skuAttributes;
    private BigDecimal price;
    private Integer quantity;
    private Boolean selected;
    private Integer stock;
}