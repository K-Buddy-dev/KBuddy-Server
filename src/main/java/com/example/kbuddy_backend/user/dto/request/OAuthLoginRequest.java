package com.example.kbuddy_backend.user.dto.request;

import com.example.kbuddy_backend.user.constant.OAuthCategory;
import jakarta.validation.constraints.Email;

public record OAuthLoginRequest(@Email String email, OAuthCategory oauth) {
    public static OAuthLoginRequest of(String email, OAuthCategory oauth) {
        return new OAuthLoginRequest(email, oauth);
    }
}
