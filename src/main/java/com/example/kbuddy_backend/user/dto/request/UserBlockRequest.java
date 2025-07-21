package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserBlockRequest(
        @NotNull(message = "차단할 사용자 ID는 필수입니다")
        Long blockedUserId
) {
} 