package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BookingListResponse(
        List<BookingSummary> content,
        long totalElements) {

    public record BookingSummary(
            Long bookingId,
            String counselorName,
            String counselorCoverImageUrl,
            String topic,
            String status,
            int totalPrice,
            LocalDateTime bookingStartUtc,
            LocalDateTime bookingEndUtc) {
    }
}
