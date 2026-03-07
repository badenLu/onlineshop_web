package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItemResponse;

import java.util.List;

public interface CartService {
    void addToCart(Long userId, Long skuId, int quantity);
    List<CartItemResponse> getCart(Long userId);
    void updateQuantity(Long userId, Long skuId, int quantity);
    void removeFromCart(Long userId, Long skuId);
    void clearCart(Long userId);
}