package com.nhnacademy.gateway.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }

    public String getRefreshToken(String userId) {
        return (String) redisBlackListTemplate.opsForValue().get(userId);
    }
}
