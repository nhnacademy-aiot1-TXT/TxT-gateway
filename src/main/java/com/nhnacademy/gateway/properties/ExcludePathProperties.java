package com.nhnacademy.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("filter.exclude")
@Getter
@Setter
public class ExcludePathProperties {
    private String path;
}
