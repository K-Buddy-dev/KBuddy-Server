package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAvailabilityRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime) {
}
