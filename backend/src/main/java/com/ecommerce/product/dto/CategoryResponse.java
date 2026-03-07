package com.ecommerce.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private String iconUrl;
    private List<CategoryResponse> children;
}
