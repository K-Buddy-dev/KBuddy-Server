package com.example.kbuddy_backend.user.dto.request;

import com.example.kbuddy_backend.user.constant.OAuthCategory;
import jakarta.validation.constraints.Email;

public record OAuthLoginRequest(String oauthUid, OAuthCategory oauth) {
    public static OAuthLoginRequest of(String oauthUid, OAuthCategory oauth) {
        return new OAuthLoginRequest(oauthUid, oauth);
    }
}
