package com.example.kbuddy_backend.qna.dto.request;

import jakarta.validation.constraints.NotNull;

public record QnaPlusHeartRequest(
        @NotNull(message = "QnA ID는 필수입니다")
        Long qnaId) {
}
