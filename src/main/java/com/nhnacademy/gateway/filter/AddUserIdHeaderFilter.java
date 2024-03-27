package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.properties.ExcludePathProperties;
import com.nhnacademy.gateway.properties.JwtProperties;
import com.nhnacademy.gateway.utils.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class AddUserIdHeaderFilter extends AbstractGatewayFilterFactory<AddUserIdHeaderFilter.Config> implements Ordered {
    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;
    private final List<String> excludePathList;

    public AddUserIdHeaderFilter(
            JwtProperties jwtProperties,
            JwtProvider jwtProvider,
            ExcludePathProperties excludePathProperties
    ) {
        super(Config.class);
        this.jwtProperties = jwtProperties;
        this.jwtProvider = jwtProvider;
        this.excludePathList = List.of(excludePathProperties.getPath().split(","));
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("add user id header filter");
            ServerHttpRequest request = exchange.getRequest();
            URI uri = request.getURI();

            if (excludePathList.contains(uri.getPath())) {
                return chain.filter(exchange);
            }

            String accessToken = jwtProvider.extractAuthorizationHeader(request)
                    .replace(jwtProperties.getTokenPrefix(), "")
                    .trim();

            String userId = jwtProvider.getUserId(accessToken);

            exchange.mutate()
                    .request(builder -> builder.header("X-USER-ID", userId));

            return chain.filter(exchange);
        };
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
