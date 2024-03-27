package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.properties.JwtProperties;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 토큰 검증을 위한 Parser 및 토큰 요청을 보내기 위한 config 클래스
 *
 * @author gahyoung
 * @version 1.0
 */
@Configuration
public class CommonConfig {

    /**
     * 토큰 검증을 위한 JwtParser 빈 등록 메서드
     *
     * @param jwtProperties 토큰 정보가 담겨있는 프로퍼티
     * @return 토큰을 parsing 하기 위한 JwtParser 인스턴스 반환
     */
    @Bean
    public JwtParser jwtParser(JwtProperties jwtProperties) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8))
                .build();
    }

    /**
     * RestTemplate을 편리하게 구현하기 위한 RestTemplateBuilder 빈 등록 메서드
     *
     * @return RestTemplateBuilder 인스턴스 반환
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    /**
     * RestTemplate 빈 등록 메서드
     * 지정된 시간이 지나도 연결되지 않으면 예외가 발생하도록 Timeout 설정
     * 지정된 시간이 지나도 응답이 없다면 예외가 발생하도록 Timeout 설정
     *
     * @param builder RestTemplateBuilder 인스턴스
     * @return 지정된 타임아웃으로 구성된 RestTemplate 인스턴스 반환
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3L))
                .setReadTimeout(Duration.ofSeconds(3L))
                .build();
    }
}
