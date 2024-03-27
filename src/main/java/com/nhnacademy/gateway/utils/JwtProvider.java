package com.nhnacademy.gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * jwt 관련 작업을 처리하는 util 클래스
 *
 * @author parksangwon
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {
    /**
     * jwt를 검증하기 위해 사용하는 parser 객체
     */
    private final JwtParser jwtParser;

    /**
     * Authorization 헤더에서 token 타입과 token으로 이루어진 값을 추출하는 메서드
     *
     * @param request Authorization 헤더를 확인하기 위해 사용하는 객체
     * @return Authorization 헤더의 값
     */
    public String extractAuthorizationHeader(ServerHttpRequest request) {
        return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    }

    /**
     * 토큰에서 userId를 추출하는 메서드
     *
     * @param token token 문자열
     * @return token에서 추출한 userId 문자열
     */
    public String getUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

    /**
     * token을 검증하고 검증 결과를 반환하는 메서드
     *
     * @param token token 문자열
     * @return 검증 결과 객체
     */
    public JwtStatus validateToken(String token) {
        try {
            getClaims(token);
            return JwtStatus.ACCESS;
        } catch (ExpiredJwtException e) {
            return JwtStatus.EXPIRED;
        } catch (SignatureException e) {
            return JwtStatus.INVALID;
        }

    }

    /**
     * token에서 claim들을 추출하는 메서드
     *
     * @param token token 문자열
     * @return claim들로 구성된 객체
     */
    private Claims getClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }
}
