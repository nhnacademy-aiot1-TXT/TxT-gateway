package com.nhnacademy.gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtParser jwtParser;

    public String extractAuthorizationHeader(ServerHttpRequest request) {
        return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    }

    public String getUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

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

    private Claims getClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }
}
