package com.example.kbuddy_backend.user.dto.response;

public record EmailCodeResponse(String email, String code) {
    public static EmailCodeResponse of(String email, String code) {
        return new EmailCodeResponse(email, code);
    }
}

