package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.filter.AddUserIdHeaderFilter;
import com.nhnacademy.gateway.filter.AuthorizationHeaderCheckFilter;
import com.nhnacademy.gateway.filter.VerificationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteLocatorConfig {

    private final AuthorizationHeaderCheckFilter authorizationHeaderCheckFilter;
    private final VerificationTokenFilter verificationTokenFilter;
    private final AddUserIdHeaderFilter addUserIdHeaderFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                      .route("user-management", p -> p.path("/api/user/**")
                                                      .filters(f -> f.filter(authorizationHeaderCheckFilter.apply(new AuthorizationHeaderCheckFilter.Config()))
                                                                     .filter(verificationTokenFilter.apply(new VerificationTokenFilter.Config()))
                                                                     .filter(addUserIdHeaderFilter.apply(new AddUserIdHeaderFilter.Config())))
                                                      .uri("http://USER-MANAGEMENT"))
                      .route("authorization-server", p -> p.path("/api/auth/**")
                                                           .filters(f -> f.filter(authorizationHeaderCheckFilter.apply(new AuthorizationHeaderCheckFilter.Config()))
                                                                          .filter(verificationTokenFilter.apply(new VerificationTokenFilter.Config()))
                                                                          .filter(addUserIdHeaderFilter.apply(new AddUserIdHeaderFilter.Config())))
                                                           .uri("http://AUTHORIZATION-SERVER"))
                      .build();
    }
}
