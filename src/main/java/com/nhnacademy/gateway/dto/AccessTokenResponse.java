package com.nhnacademy.gateway.dto;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * 액세스 토큰 응답 DTO 클래스
 */
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;

    /**
     * 응답 헤더에 사용할 토큰타입과 엑세스 토큰으로 구성된 문자열을 반환하는 메서드
     *
     * @return 토큰타입과 엑세스 토큰 정보를 담은 문자열 반환
     */
    public String toHeader(){
        return tokenType + " " + accessToken;
    }
}
