package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CounselorBookingListResponse(
        List<BookingSummary> content,
        long totalElements) {

    public record BookingSummary(
            Long bookingId,
            String customerName,
            String customerUsername,
            String topic,
            LocalDate birthDate,
            String status,
            int totalPrice,
            LocalDateTime bookingStartUtc,
            LocalDateTime bookingEndUtc) {
    }
}
