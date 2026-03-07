package com.ecommerce.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer salesCount;
    private String mainImage;
    private List<String> images;
    private String status;
    private List<SkuResponse> skus;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class SkuResponse {
        private Long id;
        private String skuCode;
        private Map<String, String> attributes;
        private BigDecimal price;
        private Integer stock;
    }
}
