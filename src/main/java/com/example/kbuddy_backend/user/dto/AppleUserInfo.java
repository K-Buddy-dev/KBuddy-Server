package com.example.kbuddy_backend.user.dto;

public record AppleUserInfo(
    AppleUserName name
) {
    public record AppleUserName(
        String firstName,
        String lastName
    ) {}
}