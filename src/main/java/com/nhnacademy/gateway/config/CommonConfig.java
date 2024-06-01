package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.exception.CryptoOperationException;
import com.nhnacademy.gateway.properties.JwtProperties;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

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
     * @return 토큰을 parsing 하기 위한 JwtParser 인스턴스 반환
     */
    @Bean
    public JwtParser jwtParser() {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKeyDecryption(null))
                .build();
    }

    /**
     * RSA 공개키 생성 빈 등록 메서드
     *
     * @param jwtProperties jwt 설정 값
     * @return 복혹화된 public key
     */
    @Bean
    public PublicKey getPublicKeyDecryption(JwtProperties jwtProperties) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoOperationException("JwtService: " + e);
        }
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
