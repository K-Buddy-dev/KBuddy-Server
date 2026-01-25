package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
        @NotNull Long bookingId,
        @Min(1) @Max(5) int rating,
        String comment) {
}
