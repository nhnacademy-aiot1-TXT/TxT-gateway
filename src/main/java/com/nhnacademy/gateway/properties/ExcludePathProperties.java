package com.nhnacademy.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * filter를 적용하지 않는 path를 저장하기 위한 클래스
 *
 * @author parksangwon
 * @version 1.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties("filter.exclude")
public class ExcludePathProperties {
    private String path;
}
