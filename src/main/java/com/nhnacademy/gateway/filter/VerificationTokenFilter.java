package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.client.UserAdaptor;
import com.nhnacademy.gateway.dto.AccessTokenResponse;
import com.nhnacademy.gateway.utils.ExceptionUtil;
import com.nhnacademy.gateway.utils.JwtProvider;
import com.nhnacademy.gateway.utils.JwtStatus;
import com.nhnacademy.gateway.utils.RedisUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VerificationTokenFilter extends AbstractGatewayFilterFactory<VerificationTokenFilter.Config> implements Ordered {
    private static final String AUTHORIZATION = "Authorization";
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;
    private final ExceptionUtil exceptionUtil;
    private final UserAdaptor userAdaptor;

    public VerificationTokenFilter(JwtProvider jwtProvider, RedisUtil redisUtil, UserAdaptor userAdaptor, ExceptionUtil exceptionUtil) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
        this.redisUtil = redisUtil;
        this.exceptionUtil = exceptionUtil;
        this.userAdaptor = userAdaptor;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String accessToken = jwtProvider.extractAuthorizationHeader(exchange.getRequest());

            if(redisUtil.hasKey(accessToken)){
                return exceptionUtil.exceptionHandler(exchange, "user already logout");
            }

            JwtStatus status = jwtProvider.validateToken(accessToken);
            switch (status){
                case EXPIRED:
                    HttpCookie refreshTokenCookie = exchange.getRequest().getCookies().getFirst("refreshToken");
                    String refreshTokenValue = refreshTokenCookie.getValue();

                    if(!jwtProvider.validateToken(refreshTokenValue).equals(JwtStatus.ACCESS)){
                        return exceptionUtil.exceptionHandler(exchange, "refresh token not valid");
                    }

                    String refreshUserId = jwtProvider.getUserId(refreshTokenValue);
                    AccessTokenResponse accessTokenResponse = userAdaptor.reissueToken(refreshUserId, refreshTokenValue);

                    exchange.mutate().request(builder -> {
                        builder.header(AUTHORIZATION, accessTokenResponse.toHeader());
                    });

                    ServerHttpResponse response = exchange.getResponse();
                    response.beforeCommit(()->{
                        response.getHeaders().set(AUTHORIZATION, accessTokenResponse.toHeader());
                        return Mono.empty();
                    });

                case INVALID:
                    return exceptionUtil.exceptionHandler(exchange, "access token not valid");

            }

            return chain.filter(exchange);
        };
    }


    @Override
    public int getOrder() {
        return 1;
    }

}
