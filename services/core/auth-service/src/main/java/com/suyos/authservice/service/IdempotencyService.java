package com.suyos.authservice.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service for handling idempotency of POST requests using Redis.
 * 
 * <p>Provides methods to check if a request is already in progress, store
 * completed responses, and retrieve cached responses.</p>
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String IN_PROGRESS = "IN_PROGRESS";

    /**
     * Checks if a key already exists in Redis.
     *
     * @param key Idempotency key
     * @return true if key exists, false otherwise
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Stores a value in Redis with a TTL.
     *
     * @param key   Idempotency key
     * @param value Serialized response
     * @param ttl   Time-to-live duration
     */
    public void store(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * Retrieves a value from Redis.
     *
     * @param key Idempotency key
     * @return Cached value, or null if not present
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Attempts to mark a request as in-progress atomically.
     * Returns true if the request can proceed (key did not exist),
     * false if a duplicate is detected.
     *
     * @param key Idempotency key
     * @param ttl Time-to-live for in-progress mark
     * @return true if processing can start, false if duplicate
     */
    public boolean checkAndLock(String key, Duration ttl) {
        return Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(key, IN_PROGRESS, ttl)
        );
    }

    /**
     * Marks a request as complete by storing the serialized response.
     *
     * @param key   Idempotency key
     * @param value Serialized response
     * @param ttl   Time-to-live for completed request
     */
    public void markComplete(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }
    
}