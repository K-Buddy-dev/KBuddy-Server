package com.example.kbuddy_backend.payment.constant;

/** 멱등키의 결제 작업 종류를 구분해 다른 작업에서 같은 키가 재사용되는 것을 막는다. */
public enum PaymentIdempotencyOperation {
    CONFIRM_DEPOSIT
}
