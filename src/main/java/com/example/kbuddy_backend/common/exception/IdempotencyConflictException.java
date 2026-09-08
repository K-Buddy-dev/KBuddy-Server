package com.example.kbuddy_backend.common.exception;

/** 같은 멱등키로 하나의 결과를 안전하게 보장할 수 없을 때 발생하는 충돌 예외다. */
public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
