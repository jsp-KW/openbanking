package com.fintech.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisTestService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveTest() {
        redisTemplate.opsForValue().set("testKey", "Hi redis~!");
    }

    public String getTest() {
        return (String) redisTemplate.opsForValue().get("testKey");
    }
}
