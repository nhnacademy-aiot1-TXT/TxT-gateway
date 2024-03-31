package com.nhnacademy.gateway.exception;

public class CryptoOperationException extends RuntimeException{
    public CryptoOperationException(String message) {
        super(message);
    }
}
