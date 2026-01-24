package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotNull;

public record BookingReserveRequest(
        @NotNull String counselorId,
        @NotNull Long availabilityId) {
}
