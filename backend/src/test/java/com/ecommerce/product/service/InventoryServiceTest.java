package com.ecommerce.product.service;

import com.ecommerce.product.entity.ProductSku;
import com.ecommerce.product.repository.ProductSkuRepository;
import com.ecommerce.product.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Tests")
class InventoryServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ProductSkuRepository productSkuRepository;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    @DisplayName("Should deduct stock successfully when sufficient")
    void deductStock_Success() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(1L);

        boolean result = inventoryService.deductStock(1L, 5);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should fail to deduct when stock insufficient")
    void deductStock_Insufficient() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(0L);

        boolean result = inventoryService.deductStock(1L, 100);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should warm up stock on cache miss then retry")
    void deductStock_CacheMiss_ThenRetry() {
        // First call returns -1 (cache miss), second call returns 1 (success)
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(-1L)
                .thenReturn(1L);

        ProductSku sku = ProductSku.builder().id(1L).stock(50).build();
        when(productSkuRepository.findById(1L)).thenReturn(Optional.of(sku));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        boolean result = inventoryService.deductStock(1L, 5);

        assertThat(result).isTrue();
        verify(valueOperations).set(eq("inventory:sku:1"), eq("50"), anyLong(), any());
    }

    @Test
    @DisplayName("Should restore stock in Redis")
    void restoreStock_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        inventoryService.restoreStock(1L, 5);

        verify(valueOperations).increment("inventory:sku:1", 5);
    }
}