package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDateTime;

public record BookingReserveResponse(
        Long bookingId,
        String status,
        int slotCount,
        int totalPrice,
        LocalDateTime bookingStartUtc,
        LocalDateTime bookingEndUtc,
        Long paymentId,
        String bankName,
        String accountNumber,
        String accountHolder,
        LocalDateTime depositDueAt) {
}
