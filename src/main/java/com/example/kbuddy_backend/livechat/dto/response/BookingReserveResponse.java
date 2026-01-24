package com.example.kbuddy_backend.livechat.dto.response;

import java.time.Instant;

public record BookingReserveResponse(
        Long bookingId,
        String status,
        Instant holdExpiresAt) {
}
