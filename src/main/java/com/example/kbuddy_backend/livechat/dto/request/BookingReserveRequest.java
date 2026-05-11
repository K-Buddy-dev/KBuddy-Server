package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingReserveRequest(
        @NotNull Long counselorId,
        @NotEmpty List<Long> slotIds) {
}
