package com.example.kbuddy_backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank String id,
        @NotBlank String password
) {
}
