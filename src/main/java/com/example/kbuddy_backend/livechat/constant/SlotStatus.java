package com.example.kbuddy_backend.livechat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlotStatus {
    AVAILABLE("예약 가능"),
    BOOKED("예약됨"),
    BLOCKED("차단됨");

    private final String description;
}
