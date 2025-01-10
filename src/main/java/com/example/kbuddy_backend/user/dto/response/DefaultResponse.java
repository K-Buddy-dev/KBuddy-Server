package com.example.kbuddy_backend.user.dto.response;

public record DefaultResponse(boolean status, String message) {
    public static DefaultResponse of(boolean status, String message) {
        return new DefaultResponse(status, message);
    }
}
