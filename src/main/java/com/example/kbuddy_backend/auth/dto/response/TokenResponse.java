package com.example.kbuddy_backend.auth.dto.response;

public record TokenResponse(String token) {
    public static TokenResponse from(String token) {
        return new TokenResponse(token);
    }
}
