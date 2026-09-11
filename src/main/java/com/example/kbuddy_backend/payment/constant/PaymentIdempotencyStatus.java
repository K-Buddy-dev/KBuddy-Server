package com.example.kbuddy_backend.payment.constant;

/** 멱등 요청이 실행 중인지, 최초 응답까지 저장을 마쳤는지 나타낸다. */
public enum PaymentIdempotencyStatus {
    PROCESSING,
    COMPLETED
}
