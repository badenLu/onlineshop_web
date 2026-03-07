package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.service.CartService;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductSku;
import com.ecommerce.product.repository.ProductSkuRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductSkuRepository productSkuRepository;
    private final ObjectMapper objectMapper;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRE_DAYS = 30;

    @Override
    public void addToCart(Long userId, Long skuId, int quantity) {
        // Verify SKU exists and has stock
        ProductSku sku = productSkuRepository.findById(skuId)
                .orElseThrow(() -> BusinessException.notFound("SKU"));

        if (sku.getStock() < quantity) {
            throw BusinessException.insufficientStock();
        }

        String key = CART_KEY_PREFIX + userId;
        String field = String.valueOf(skuId);

        // If already in cart, add to existing quantity
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing != null) {
            CartItem item = deserialize(existing);
            item.setQuantity(item.getQuantity() + quantity);
            redisTemplate.opsForHash().put(key, field, serialize(item));
        } else {
            CartItem item = new CartItem(skuId, quantity, true);
            redisTemplate.opsForHash().put(key, field, serialize(item));
        }

        redisTemplate.expire(key, CART_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("Added to cart: userId={}, skuId={}, quantity={}", userId, skuId, quantity);
    }

    @Override
    public List<CartItemResponse> getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<CartItemResponse> result = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long skuId = Long.valueOf((String) entry.getKey());
            CartItem item = deserialize((String) entry.getValue());

            productSkuRepository.findById(skuId).ifPresent(sku -> {
                Product product = sku.getProduct();
                result.add(CartItemResponse.builder()
                        .skuId(sku.getId())
                        .productId(product.getId())
                        .productName(product.getName())
                        .productImage(product.getMainImage())
                        .skuCode(sku.getSkuCode())
                        .skuAttributes(sku.getAttributes())
                        .price(sku.getPrice())
                        .quantity(item.getQuantity())
                        .selected(item.getSelected())
                        .stock(sku.getStock())
                        .build());
            });
        }

        return result;
    }

    @Override
    public void updateQuantity(Long userId, Long skuId, int quantity) {
        String key = CART_KEY_PREFIX + userId;
        String field = String.valueOf(skuId);

        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing == null) {
            throw BusinessException.notFound("Cart item");
        }

        CartItem item = deserialize(existing);
        item.setQuantity(quantity);
        redisTemplate.opsForHash().put(key, field, serialize(item));
    }

    @Override
    public void removeFromCart(Long userId, Long skuId) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, String.valueOf(skuId));
    }

    @Override
    public void clearCart(Long userId) {
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }

    // --- Internal cart item structure stored in Redis ---

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class CartItem {
        private Long skuId;
        private Integer quantity;
        private Boolean selected;
    }

    private String serialize(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cart item", e);
        }
    }

    private CartItem deserialize(String json) {
        try {
            return objectMapper.readValue(json, CartItem.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize cart item", e);
        }
    }
}