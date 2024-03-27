package com.nhnacademy.gateway.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis 관련 작업을 처리하는 util 클래스
 *
 * @author parksangwon
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {
    /**
     * redis를 사용하기 위한 객체
     */
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    /**
     * @param key redis에 key가 존재하는지 확인하기 위한 문자열
     * @return key가 존재하면 true, 존재하지 않으면 false
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }
}
