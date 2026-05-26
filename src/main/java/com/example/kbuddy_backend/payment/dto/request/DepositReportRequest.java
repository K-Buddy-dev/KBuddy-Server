package com.example.kbuddy_backend.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DepositReportRequest(
        @NotBlank String depositorName,
        String memo
) {
}
