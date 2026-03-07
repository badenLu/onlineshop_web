package com.ecommerce.product.service;

public interface InventoryService {
    boolean deductStock(Long skuId, int quantity);
    void restoreStock(Long skuId, int quantity);
    void warmUpStock(Long skuId);
}