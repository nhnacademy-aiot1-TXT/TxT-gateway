package com.nhnacademy.gateway.filter;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

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
    private final List<String> excludePathList;

    /**
     * filter에 필요한 객체를 주입받기 위한 생성자
     *
     * @param jwtProvider           jwt 관련 작업을 처리하기 위한 객체
     * @param jwtProperties         jwt 관련 정보를 가지고 있는 객체
     * @param redisUtil             redis 관련 작업을 처리하기 위한 객체
     * @param exceptionUtil         예외 관련 작업을 처리하기 위한 객체
     * @param excludePathProperties filter를 적용하지 않는 path를 가지고 있는 객체
     */
    public VerificationTokenFilter(
            JwtProvider jwtProvider,
            JwtProperties jwtProperties,
            RedisUtil redisUtil,
            ExceptionUtil exceptionUtil,
            ExcludePathProperties excludePathProperties) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.redisUtil = redisUtil;
        this.exceptionUtil = exceptionUtil;
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
                return exceptionUtil.exceptionHandler(exchange, HttpStatus.UNAUTHORIZED,"user already logout");
            }

            JwtStatus status = jwtProvider.validateToken(accessToken);
            switch (status) {
                case ACCESS:
                    break;
                case EXPIRED:
                    return exceptionUtil.exceptionHandler(exchange, HttpStatus.UNAUTHORIZED,"access token expired");
                case INVALID:
                    return exceptionUtil.exceptionHandler(exchange, HttpStatus.BAD_REQUEST,"access token not valid");

            }

            return chain.filter(exchange);
        };
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
