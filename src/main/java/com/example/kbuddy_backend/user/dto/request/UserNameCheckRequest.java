package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserNameCheckRequest(@NotNull String userId) {
    public static UserNameCheckRequest of(String userId) {
        return new UserNameCheckRequest(userId);
    }
}
