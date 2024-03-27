package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.client.UserAdaptor;
import com.nhnacademy.gateway.dto.AccessTokenResponse;
import com.nhnacademy.gateway.properties.ExcludePathProperties;
import com.nhnacademy.gateway.properties.JwtProperties;
import com.nhnacademy.gateway.utils.ExceptionUtil;
import com.nhnacademy.gateway.utils.JwtProvider;
import com.nhnacademy.gateway.utils.JwtStatus;
import com.nhnacademy.gateway.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * jwt token 검증 작업을 하는 필터
 *
 * @author parksangwon
 * @version 1.0
 */
@Slf4j
@Component
public class VerificationTokenFilter extends AbstractGatewayFilterFactory<VerificationTokenFilter.Config> implements Ordered {
    private static final String AUTHORIZATION = "Authorization";
    private static final String REFRESH_TOKEN = "Refresh-Token";
    private static final String SPLIT_STRING = ",";
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RedisUtil redisUtil;
    private final ExceptionUtil exceptionUtil;
    private final UserAdaptor userAdaptor;
    private final List<String> excludePathList;

    /**
     * filter에 필요한 객체를 주입받기 위한 생성자
     *
     * @param jwtProvider jwt 관련 작업을 처리하기 위한 객체
     * @param jwtProperties jwt 관련 정보를 가지고 있는 객체
     * @param redisUtil redis 관련 작업을 처리하기 위한 객체
     * @param userAdaptor 재발급 관련 작업을 처리하기 위한 객체
     * @param exceptionUtil 예외 관련 작업을 처리하기 위한 객체
     * @param excludePathProperties filter를 적용하지 않는 path를 가지고 있는 객체
     */
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
        this.excludePathList = List.of(excludePathProperties.getPath().split(SPLIT_STRING));
    }

    public static class Config {
    }

    /**
     * filter를 적용하지 않는 path를 무시하고,
     * 요청의 Authorization 헤더에서 accessToken을 추출하고,
     * redisUtil을 통해 logout을 한 유저인지 확인하고,
     * jwt token 검증을 통해 통과시키거나 재발급을 하거나 예외처리를 하는 메서드
     *
     * @return filter 로직이 담긴 람다식
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("verification token filter");
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

    /**
     * refresh token에서 userId를 추출하고,
     * userAdaptor를 통해 인증서버에 재발급을 요청하여 access token을 받고,
     * 요청의 Authorization 헤더에 새로운 access token을 넣어주는 메서드
     *
     * @param exchange request를 가져오기 위해 사용되는 객체
     * @param refreshToken refresh token 문자열
     * @return 재발급 된 access token이 담긴 dto
     */
    private AccessTokenResponse reissueToken(ServerWebExchange exchange, String refreshToken) {
        String refreshUserId = jwtProvider.getUserId(refreshToken);
        AccessTokenResponse accessTokenResponse = userAdaptor.reissueToken(refreshUserId, refreshToken);

        exchange.mutate().request(builder -> builder.header(AUTHORIZATION, accessTokenResponse.toHeader()));
        return accessTokenResponse;
    }


    /**
     * filter의 순서를 정하기 위한 메서드
     *
     * @return 순서를 나타내는 정수
     */
    @Override
    public int getOrder() {
        return 1;
    }

}
