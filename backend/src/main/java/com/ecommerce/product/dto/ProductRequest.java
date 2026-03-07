package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotBlank(message = "Main image is required")
    private String mainImage;

    private List<String> images;

    private List<SkuRequest> skus;

    @Data
    public static class SkuRequest {
        @NotBlank
        private String skuCode;

        @NotNull
        private Map<String, String> attributes;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal price;

        @NotNull
        @Min(0)
        private Integer stock;

    }
}
