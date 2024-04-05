package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.properties.JwtProperties;
import com.nhnacademy.gateway.utils.ExceptionUtil;
import com.nhnacademy.gateway.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * admin 권한을 확인하는 필터
 */
@Component
@RequiredArgsConstructor
public class AuthorizationCheckFilter extends AbstractGatewayFilterFactory<AuthorizationCheckFilter.Config> implements Ordered {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final ExceptionUtil exceptionUtil;

    public static class Config {
    }

    /**
     * /api/../admin/.. 패턴의 path로 요청을 보냈다면
     * 클라이언트의 access token payload를 확인하여 admin 권한을 가지고 있는지 확인합니다.
     * admin 권한이 아니라면 예외를 반환합니다.
     *
     * @return filter 로직이 담긴 람다식
     */
    @Override
    public GatewayFilter apply(AuthorizationCheckFilter.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            Pattern pattern = Pattern.compile("/api/.*/admin/.*");
            if (!pattern.matcher(request.getURI().getPath()).matches()) {
                return chain.filter(exchange);
            }

            String accessToken = jwtProvider.extractAuthorizationHeader(request)
                                            .replace(jwtProperties.getTokenPrefix(), "")
                                            .trim();

            String authority = jwtProvider.getClaims(accessToken).get("authority", String.class);

            if (!"ROLE_ADMIN".equals(authority)) {
                return exceptionUtil.exceptionHandler(exchange, HttpStatus.FORBIDDEN, "forbidden");
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
        return 3;
    }
}
