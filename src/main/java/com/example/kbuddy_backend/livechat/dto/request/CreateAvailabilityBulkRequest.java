package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAvailabilityBulkRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime) {
}
