package com.example.kbuddy_backend.qna.dto.request;

import jakarta.validation.constraints.NotNull;

public record BookmarkRequest(
        @NotNull(message = "컬렉션 ID는 필수입니다")
        Long collectionId) {
}
