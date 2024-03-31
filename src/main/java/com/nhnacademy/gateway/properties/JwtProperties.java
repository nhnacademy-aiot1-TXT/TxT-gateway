package com.nhnacademy.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * jwt관련 기능을 수행할 때 필요한 정보를 저장하기 위한 클래스
 *
 * @author parksangwon
 * @version 1.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secret;
    private String tokenPrefix;
}
