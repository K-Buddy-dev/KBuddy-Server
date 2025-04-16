package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotNull;

public record BlogPlusHeartRequest(
        @NotNull(message = "Blog ID는 필수입니다.")
        Long blogId) {
}
