package com.example.kbuddy_backend.livechat.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAvailabilityBulkRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate startDate,
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate endDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime) {
}
