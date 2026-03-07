package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProduct(Long id);
    Page<ProductResponse> listProducts(int page, int size, Long categoryId, String sort);
    Page<ProductResponse> searchProducts(String keyword, int page, int size);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}