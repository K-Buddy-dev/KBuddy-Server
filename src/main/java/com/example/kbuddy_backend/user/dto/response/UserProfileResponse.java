package com.example.kbuddy_backend.user.dto.response;

public record UserProfileResponse(String bio, String userId, String profileImageUrl) {
    public static UserProfileResponse of(String bio, String userId, String profileImageUrl) {
        return new UserProfileResponse(bio, userId, profileImageUrl);
    }
}
