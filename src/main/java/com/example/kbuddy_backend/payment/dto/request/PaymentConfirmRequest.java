package com.example.kbuddy_backend.payment.dto.request;

public record PaymentConfirmRequest(
        Integer confirmedAmount,
        String adminMemo
) {
}
