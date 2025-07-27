package com.example.kbuddy_backend.user.dto.response;

public record AppleLoginResponse(
    String accessToken,
    String refreshToken,
    String email,
    String oAuthUid,
    String firstName,
    String lastName,
    boolean isNew
) {
} 