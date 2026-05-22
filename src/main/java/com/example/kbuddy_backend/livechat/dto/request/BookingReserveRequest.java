package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record BookingReserveRequest(
        @NotEmpty @Size(min = 1, max = 8) List<Long> slotIds,
        @NotBlank @Size(max = 200) String topic,
        @Size(max = 500) String memo) {
}
