package com.nhnacademy.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 토큰 정보를 Redis에 저장하고 조회하기 위한 config 클래스
 *
 * @author gahyoung
 * @version 1.0
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate 생성하는 빈 등록 메서드
     * RedisConnectionFactory를 설정하여 RedisTemplate이 사용할 연결을 제공
     * Redis의 Key, Value, Hash Key, Hash Value에 사용될 Serializer 설정
     *
     * @param redisConnectionFactory Redis와의 연결을 관리하는데 사용
     * @return 구성된 RedisTemplate 인스턴스 반환
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}
