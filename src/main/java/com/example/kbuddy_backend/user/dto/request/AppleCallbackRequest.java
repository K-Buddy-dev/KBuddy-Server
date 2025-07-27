package com.example.kbuddy_backend.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleCallbackRequest(
    String code,
    @JsonProperty("id_token")
    String idToken,
    String user
) {
} 