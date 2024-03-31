package com.nhnacademy.gateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 필터에서 발생한 예외를 처리하는 util 클래스
 *
 * @author parksangwon
 * @version 1.0
 */
@Slf4j
@Component
public class ExceptionUtil {
    /**
     * 필터에서 발생한 예외를 출력하고 response에 unauthorized 상태를 적용하는 메서드
     *
     * @param exchange response를 가져오기 위해 사용하는 객체
     * @param message 예외가 발생된 이유를 출력 하기 위한 문자열
     * @return 변경한 response를 Mono 객체 형태로 응답
     */
    public Mono<Void> exceptionHandler(ServerWebExchange exchange, HttpStatus status, String message) {
        log.debug("exception message: {}", message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);

        return response.setComplete();
    }
}
