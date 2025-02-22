package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull String email, @NotNull String password) {
    public static LoginRequest of(String email, String password) {
        return new LoginRequest(email, password);
    }
}
