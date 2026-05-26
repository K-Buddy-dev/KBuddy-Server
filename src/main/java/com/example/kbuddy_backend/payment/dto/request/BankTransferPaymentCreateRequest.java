package com.example.kbuddy_backend.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record BankTransferPaymentCreateRequest(
        @NotNull Long bookingId,
        String depositorName
) {
}
