package com.example.kbuddy_backend.user.dto.response;

public record EmailCodeResponse(String email, int code) {
    public static EmailCodeResponse of(String email, int code) {
        return new EmailCodeResponse(email, code);
    }
}

