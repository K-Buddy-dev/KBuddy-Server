package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateInquiryRequest(
        @NotBlank String title,
        @NotBlank String content,
        boolean isSecret) {
}
