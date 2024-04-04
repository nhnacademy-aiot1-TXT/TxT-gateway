package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.filter.AddUserIdHeaderFilter;
import com.nhnacademy.gateway.filter.AuthorizationHeaderCheckFilter;
import com.nhnacademy.gateway.filter.AuthorizationCheckFilter;
import com.nhnacademy.gateway.filter.VerificationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 게이트웨이에 요청이 있을 때 요청을 라우팅하는 config 클래스
 *
 * @author gahyoung
 * @version 1.0
 */
@Configuration
@RequiredArgsConstructor
public class RouteLocatorConfig {

    private final AuthorizationHeaderCheckFilter authorizationHeaderCheckFilter;
    private final VerificationTokenFilter verificationTokenFilter;
    private final AddUserIdHeaderFilter addUserIdHeaderFilter;
    private final AuthorizationCheckFilter authorizationCheckFilter;

    /**
     * 요청을 라우팅하는 경로를 정의하고 필터를 적용하여 대상 서비스로 라우팅하는 메서드
     * uri는 Eureka 서버에 등록된 Eureka Client name을 사용
     * 필터는 토큰 헤더 검증, 토큰 유효성 검증, 요청에 X-USER-ID 헤더를 추가하는 작업, 인가 수행
     *
     * @param builder RouteLocatorBuilder 인스턴스
     * @return RouteLocator 인스턴스
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                      .route("user-management", p -> p.path("/api/user/**")
                                                      .filters(f -> f.filter(authorizationHeaderCheckFilter.apply(new AuthorizationHeaderCheckFilter.Config()))
                                                                     .filter(verificationTokenFilter.apply(new VerificationTokenFilter.Config()))
                                                                     .filter(addUserIdHeaderFilter.apply(new AddUserIdHeaderFilter.Config()))
                                                                     .filter(authorizationCheckFilter.apply(new AuthorizationCheckFilter.Config())))
                                                      .uri("lb://USER-MANAGEMENT"))
                      .route("authorization-server", p -> p.path("/api/auth/**")
                                                           .filters(f -> f.filter(authorizationHeaderCheckFilter.apply(new AuthorizationHeaderCheckFilter.Config()))
                                                                          .filter(verificationTokenFilter.apply(new VerificationTokenFilter.Config()))
                                                                          .filter(addUserIdHeaderFilter.apply(new AddUserIdHeaderFilter.Config()))
                                                                          .filter(authorizationCheckFilter.apply(new AuthorizationCheckFilter.Config())))
                                                           .uri("lb://AUTHORIZATION-SERVER"))
                      .build();
    }
}
