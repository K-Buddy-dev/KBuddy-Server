package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserNameCheckRequest(@NotNull String userName) {
    public static UserNameCheckRequest of(String userName) {
        return new UserNameCheckRequest(userName);
    }
}
