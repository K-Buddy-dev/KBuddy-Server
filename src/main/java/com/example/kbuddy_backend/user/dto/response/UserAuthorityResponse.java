package com.example.kbuddy_backend.user.dto.response;

public record UserAuthorityResponse(String role) {

    public static UserAuthorityResponse of(String role) {
        return new UserAuthorityResponse(role);
    }
}
