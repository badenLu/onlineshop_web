package com.ecommerce.backend.config;

import com.ecommerce.order.dto.OrderEvent;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
public class TestInfrastructureConfig {

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return Mockito.mock(StringRedisTemplate.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    // Elasticsearch
    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchOperations() {
        return Mockito.mock(ElasticsearchOperations.class);
    }

    // RedissonClient
    @Bean
    @Primary
    public RedissonClient redissonClient() {
        return Mockito.mock(RedissonClient.class);
    }
}