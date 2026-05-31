package com.example.kbuddy_backend.livechat.dto.response;

import java.time.Instant;
import java.util.List;

public record CounselorAvailabilityResponse(
        List<AvailabilitySlot> slots) {
    public record AvailabilitySlot(
            Long availabilityId,
            Instant slotStartUtc,
            String status) {
    }
}
