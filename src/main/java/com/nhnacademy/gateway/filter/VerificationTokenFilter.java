package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.client.UserAdaptor;
import com.nhnacademy.gateway.dto.AccessTokenResponse;
import com.nhnacademy.gateway.properties.ExcludePathProperties;
import com.nhnacademy.gateway.properties.JwtProperties;
import com.nhnacademy.gateway.utils.ExceptionUtil;
import com.nhnacademy.gateway.utils.JwtProvider;
import com.nhnacademy.gateway.utils.JwtStatus;
import com.nhnacademy.gateway.utils.RedisUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class VerificationTokenFilter extends AbstractGatewayFilterFactory<VerificationTokenFilter.Config> implements Ordered {
    private static final String AUTHORIZATION = "Authorization";
    private static final String REFRESH_TOKEN = "Refresh-Token";
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RedisUtil redisUtil;
    private final ExceptionUtil exceptionUtil;
    private final UserAdaptor userAdaptor;
    private final List<String> excludePathList;

    public VerificationTokenFilter(
            JwtProvider jwtProvider,
            JwtProperties jwtProperties,
            RedisUtil redisUtil,
            UserAdaptor userAdaptor,
            ExceptionUtil exceptionUtil,
            ExcludePathProperties excludePathProperties
    ) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.redisUtil = redisUtil;
        this.exceptionUtil = exceptionUtil;
        this.userAdaptor = userAdaptor;
        this.excludePathList = List.of(excludePathProperties.getPath().split(","));
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (excludePathList.contains(request.getURI().getPath())) {
                return chain.filter(exchange);
            }

            String accessToken = jwtProvider.extractAuthorizationHeader(request)
                    .replace(jwtProperties.getTokenPrefix(), "")
                    .trim();
            if (redisUtil.hasKey(accessToken)) {
                return exceptionUtil.exceptionHandler(exchange, "user already logout");
            }

            JwtStatus status = jwtProvider.validateToken(accessToken);
            switch (status) {
                case ACCESS:
                    break;
                case EXPIRED:
                    String refreshToken = request
                            .getHeaders()
                            .getFirst(REFRESH_TOKEN);

                    if (!jwtProvider.validateToken(refreshToken).equals(JwtStatus.ACCESS)) {
                        return exceptionUtil.exceptionHandler(exchange, "refresh token not valid");
                    }

                    AccessTokenResponse accessTokenResponse = reissueToken(exchange, refreshToken);

                    ServerHttpResponse response = exchange.getResponse();
                    response.beforeCommit(() -> {
                        response.getHeaders().set(AUTHORIZATION, accessTokenResponse.toHeader());
                        return Mono.empty();
                    });
                    break;
                case INVALID:
                    return exceptionUtil.exceptionHandler(exchange, "access token not valid");

            }

            return chain.filter(exchange);
        };
    }

    private AccessTokenResponse reissueToken(ServerWebExchange exchange, String refreshToken) {
        String refreshUserId = jwtProvider.getUserId(refreshToken);
        AccessTokenResponse accessTokenResponse = userAdaptor.reissueToken(refreshUserId, refreshToken);

        exchange.mutate().request(builder -> builder.header(AUTHORIZATION, accessTokenResponse.toHeader()));
        return accessTokenResponse;
    }


    @Override
    public int getOrder() {
        return 1;
    }

}
