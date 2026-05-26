package com.example.kbuddy_backend.payment.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    BANK_TRANSFER("무통장 입금");

    private final String description;
}
