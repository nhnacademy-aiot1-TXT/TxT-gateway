package com.nhnacademy.gateway.client;

import com.nhnacademy.gateway.dto.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * 인증 서버로 토큰 재발급 요청을 보내는 클래스
 * @author gahyoung
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class UserAdaptor {
    private static final String X_USER_ID = "X-USER-ID";
    private final RestTemplate restTemplate;

    /**
     * 인증 서버로 토큰 재발급 요청을 보내는 메서드
     *
     * @param userId 토큰 재발급에 필요한 user ID
     * @param refreshToken 토큰 재발급에 필요한 리프레시 토큰
     * @return 재발급 받은 토큰 반환
     */
    public AccessTokenResponse reissueToken(String userId, String refreshToken){

        HttpHeaders headers = new HttpHeaders();
        headers.set(X_USER_ID, userId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> requestEntity = new HttpEntity<>(refreshToken, headers);

        //Url 미정
        HttpEntity<AccessTokenResponse> response = restTemplate.exchange(
                "",
                HttpMethod.POST,
                requestEntity,
                AccessTokenResponse.class
        );

        return response.getBody();
    }
}
