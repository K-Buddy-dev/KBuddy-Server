package com.example.kbuddy_backend.user.dto.request;

import com.example.kbuddy_backend.user.constant.OAuthCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record OAuthLoginRequest(@NotNull String oAuthUid, @NotNull OAuthCategory oAuthCategory) {
    public static OAuthLoginRequest of(String oAuthUid, OAuthCategory oAuthCategory) {
        return new OAuthLoginRequest(oAuthUid, oAuthCategory);
    }
}
