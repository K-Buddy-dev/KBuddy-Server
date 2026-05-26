package com.example.kbuddy_backend.payment.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    AWAITING_DEPOSIT("입금 대기"),
    DEPOSIT_REPORTED("입금 완료 신고"),
    PAID("입금 확인 완료"),
    EXPIRED("입금 기한 만료"),
    CANCELLED("결제 취소"),
    REFUND_PENDING("환불 대기"),
    REFUNDED("환불 완료");

    private final String description;
}
