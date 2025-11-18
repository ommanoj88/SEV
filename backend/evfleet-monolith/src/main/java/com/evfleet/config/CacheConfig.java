package com.evfleet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache Configuration
 *
 * Configures Redis-based caching for the application (when Redis is available).
 * When Redis is not available, falls back to simple in-memory caching.
 *
 * Defines cache policies for different cache regions:
 * - users: User data cached for 5 minutes (matches HTTP cache)
 * - roles: Role data cached for 1 hour (rarely changes)
 * - vehicles: Vehicle data cached for 2 minutes
 * - pricing: Pricing plans cached for 1 hour
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(RedisConnectionFactory.class)
public class CacheConfig {

    /**
     * Configure Redis Cache Manager with custom serializers and TTL
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configure ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration (5 minutes TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .disableCachingNullValues();

        // Cache-specific configurations
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("users",
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("roles",
                        defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("vehicles",
                        defaultConfig.entryTtl(Duration.ofMinutes(2)))
                .withCacheConfiguration("pricing",
                        defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("chargingStations",
                        defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("drivers",
                        defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return builder.build();
    }
}
