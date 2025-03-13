package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull String emailOrUserId, @NotNull String password) {
    public static LoginRequest of(String emailOrUserId, String password) {
        return new LoginRequest(emailOrUserId, password);
    }
}
