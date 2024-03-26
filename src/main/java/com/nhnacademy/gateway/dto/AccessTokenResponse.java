package com.nhnacademy.gateway.dto;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;

    public String toHeader(){
        return tokenType + " " + accessToken;
    }
}
