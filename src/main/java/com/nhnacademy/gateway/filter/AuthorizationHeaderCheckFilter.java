package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.properties.ExcludePathProperties;
import com.nhnacademy.gateway.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Authorization 헤더가 존재하는지, 헤더의 값이 존재하는지 확인하는 필터
 *
 * @author parksangwon
 * @version 1.0
 */
@Slf4j
@Component
public class AuthorizationHeaderCheckFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderCheckFilter.Config> implements Ordered {
    private static final String SPLIT_STRING = ",";
    private final ExceptionUtil exceptionUtil;
    private final List<String> excludePathList;

    /**
     * filter에 필요한 객체를 주입받기 위한 생성자
     *
     * @param exceptionUtil 예외 관련 작업을 처리하기 위한 객체
     * @param excludePathProperties filter를 적용하지 않는 path를 가지고 있는 객체
     */
    public AuthorizationHeaderCheckFilter(
            ExceptionUtil exceptionUtil,
            ExcludePathProperties excludePathProperties
    ) {
        super(Config.class);
        this.exceptionUtil = exceptionUtil;
        this.excludePathList = List.of(excludePathProperties.getPath().split(SPLIT_STRING));
    }

    public static class Config {
    }

    /**
     * filter를 적용하지 않는 path를 무시하고,
     * Authorization 헤더가 존재하는지, 값이 존재하는지 확인하고,
     * 존재하지 않으면 예외를 처리하는 메서드
     *
     * @return filter 로직이 담긴 람다식
     */
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
                return exceptionUtil.exceptionHandler(exchange, HttpStatus.UNAUTHORIZED ,"not exist authorization header");
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
        return 0;
    }
}
