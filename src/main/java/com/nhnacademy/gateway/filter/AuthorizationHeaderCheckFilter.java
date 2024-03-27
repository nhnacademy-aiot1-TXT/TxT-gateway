package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.properties.ExcludePathProperties;
import com.nhnacademy.gateway.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class AuthorizationHeaderCheckFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderCheckFilter.Config> implements Ordered {
    private final ExceptionUtil exceptionUtil;
    private final List<String> excludePathList;

    public AuthorizationHeaderCheckFilter(
            ExceptionUtil exceptionUtil,
            ExcludePathProperties excludePathProperties
    ) {
        super(Config.class);
        this.exceptionUtil = exceptionUtil;
        this.excludePathList = List.of(excludePathProperties.getPath().split(","));
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("authorization header check filter");
            ServerHttpRequest request = exchange.getRequest();
            URI uri = request.getURI();

            if (excludePathList.contains(uri.getPath())) {
                return chain.filter(exchange);
            }

            if (Objects.isNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)) ||
                    !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return exceptionUtil.exceptionHandler(exchange, "not exist authorization header");
            }

            return chain.filter(exchange);
        };
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
