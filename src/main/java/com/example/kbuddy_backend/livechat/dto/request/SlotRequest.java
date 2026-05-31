package com.example.kbuddy_backend.livechat.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SlotRequest(
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotEmpty List<LocalTime> times) {
}
