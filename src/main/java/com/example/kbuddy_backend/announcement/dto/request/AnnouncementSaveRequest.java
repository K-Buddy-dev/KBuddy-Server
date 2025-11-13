package com.example.kbuddy_backend.announcement.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AnnouncementSaveRequest(
        @Schema(description = "공지사항 게시글 제목", example = "공지사항 게시글 제목", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "제목 필드는 필수입니다")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        String title,

        @Schema(description = "공지사항 게시글 내용", example = "공지사항 게시글 내용", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "내용 필드는 필수입니다")
        String description
) {
    public static AnnouncementSaveRequest of(String title, String description) {
        return new AnnouncementSaveRequest(title, description);
    }
}
