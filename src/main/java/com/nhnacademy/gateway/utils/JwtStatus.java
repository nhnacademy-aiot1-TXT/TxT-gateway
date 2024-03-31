package com.nhnacademy.gateway.utils;

/**
 * jwt 검증 상태를 나타내기 위한 enum 클래스
 *
 * @author parksangwon
 * @version 1.0
 */
public enum JwtStatus {
    /**
     * 검증이 무사히 완료된 상태
     */
    ACCESS,

    /**
     * token 유효기간이 만료된 상태
     */
    EXPIRED,

    /**
     * token signature가 유효하지 않은 상태
     */
    INVALID
}
