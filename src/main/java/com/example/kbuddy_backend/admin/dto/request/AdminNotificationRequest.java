package com.example.kbuddy_backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminNotificationRequest(
        @NotBlank String title,
        @NotBlank String message,
        String targetId) {
}
