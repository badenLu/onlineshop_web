package com.ecommerce.product.service.impl;

import com.ecommerce.product.repository.ProductSkuRepository;
import com.ecommerce.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StringRedisTemplate redisTemplate;
    private final ProductSkuRepository productSkuRepository;

    private static final String STOCK_KEY_PREFIX = "inventory:sku:";

    // Lua script: atomic check-and-deduct, no race condition
    private static final String DEDUCT_STOCK_LUA = """
            local stock = redis.call('GET', KEYS[1])
            if stock == false then return -1 end
            if tonumber(stock) >= tonumber(ARGV[1]) then
                redis.call('DECRBY', KEYS[1], ARGV[1])
                return 1
            end
            return 0
            """;

    @Override
    public boolean deductStock(Long skuId, int quantity) {
        String key = STOCK_KEY_PREFIX + skuId;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(DEDUCT_STOCK_LUA, Long.class),
                List.of(key),
                String.valueOf(quantity)
        );

        if (result == null) return false;

        if (result == -1) {
            // Cache miss, warm up and retry
            warmUpStock(skuId);
            result = redisTemplate.execute(
                    new DefaultRedisScript<>(DEDUCT_STOCK_LUA, Long.class),
                    List.of(key),
                    String.valueOf(quantity)
            );
            return result != null && result == 1;
        }

        return result == 1;
    }

    @Override
    public void restoreStock(Long skuId, int quantity) {
        String key = STOCK_KEY_PREFIX + skuId;
        redisTemplate.opsForValue().increment(key, quantity);
        log.info("Stock restored in Redis: SKU={}, quantity={}", skuId, quantity);
    }

    @Override
    public void warmUpStock(Long skuId) {
        productSkuRepository.findById(skuId).ifPresent(sku -> {
            String key = STOCK_KEY_PREFIX + skuId;
            redisTemplate.opsForValue().set(key, String.valueOf(sku.getStock()), 24, TimeUnit.HOURS);
            log.info("Stock warmed up: SKU={}, stock={}", skuId, sku.getStock());
        });
    }
}