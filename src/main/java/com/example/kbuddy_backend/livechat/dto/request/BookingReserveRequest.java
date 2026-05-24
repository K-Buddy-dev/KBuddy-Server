package com.example.kbuddy_backend.livechat.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record BookingReserveRequest(
        @NotNull Long counselorId,
        @NotBlank @Size(max = 200) String topic,
        @Size(max = 500) String memo,
        @JsonFormat(pattern = "MM-dd-yyyy")
        @NotNull LocalDate birthDate,
        @NotEmpty List<Long> slotIds) {
}
