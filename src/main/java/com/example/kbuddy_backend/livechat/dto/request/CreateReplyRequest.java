package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReplyRequest(
        @NotBlank @Size(max = 1000) String content) {
}
