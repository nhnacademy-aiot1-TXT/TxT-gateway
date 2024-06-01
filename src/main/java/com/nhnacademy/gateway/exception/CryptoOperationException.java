package com.nhnacademy.gateway.exception;

/**
 * 암호화 key 생성 중 발생하는 예외를 처리하기 위한 클래스
 */
public class CryptoOperationException extends RuntimeException {
    /**
     * message를 파라미터로 가지는 생성자 메서드
     *
     * @param message 얘외 메시지
     */
    public CryptoOperationException(String message) {
        super(message);
    }
}
