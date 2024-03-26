package com.nhnacademy.gateway.client;

import com.nhnacademy.gateway.dto.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RequiredArgsConstructor
public class UserAdaptor {
    private static final String X_USER_ID = "X-USER-ID";
    private final RestTemplate restTemplate;

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
