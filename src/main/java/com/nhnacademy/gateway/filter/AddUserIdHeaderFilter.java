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

/**
 * 요청에 X-USER-ID 헤더를 추가해주는 필터
 *
 * @author parksangwon
 * @version 1.0
 */
@Slf4j
@Component
public class AddUserIdHeaderFilter extends AbstractGatewayFilterFactory<AddUserIdHeaderFilter.Config> implements Ordered {
    private static final String SPLIT_STRING = ",";
    private static final String X_USER_ID = "X-USER-ID";
    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;
    private final List<String> excludePathList;

    /**
     * filter에 필요한 객체를 주입받기 위한 생성자
     *
     * @param jwtProperties jwt 관련 정보를 가지고 있는 객체
     * @param jwtProvider jwt 관련 작업을 처리하기 위한 객체
     * @param excludePathProperties filter를 적용하지 않는 path를 가지고 있는 객체
     */
    public AddUserIdHeaderFilter(
            JwtProperties jwtProperties,
            JwtProvider jwtProvider,
            ExcludePathProperties excludePathProperties
    ) {
        super(Config.class);
        this.jwtProperties = jwtProperties;
        this.jwtProvider = jwtProvider;
        this.excludePathList = List.of(excludePathProperties.getPath().split(SPLIT_STRING));
    }

    public static class Config {
    }

    /**
     * filter를 적용하지 않는 path는 무시하고 accessToken에서 userId를 추출하여
     * 요청 헤더에 X-USER-ID로 넣어주는 작업을 하는 메서드
     *
     * @return filter 로직이 담긴 람다식
     */
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
                    .request(builder -> builder.header(X_USER_ID, userId));

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
        return 2;
    }
}
