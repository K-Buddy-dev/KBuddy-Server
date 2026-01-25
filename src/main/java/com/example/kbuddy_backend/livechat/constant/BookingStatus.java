package com.example.kbuddy_backend.livechat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingStatus {
    PENDING("결제 대기"),
    PAID("결제 완료"),
    COMPLETED("상담 완료"),
    CANCELLED("취소됨"),
    REFUNDED("환불 완료");

    private final String description;
}
