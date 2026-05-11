package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CounselorAvailabilityResponse(
        List<AvailabilitySlot> slots) {
    public record AvailabilitySlot(
            Long availabilityId,
            LocalDate date,
            LocalTime time,
            String status) {
    }
}
