package com.example.kbuddy_backend.announcement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "공지사항 게시글 업데이트 요청")
public record AnnouncementUpdateRequest(

        @Schema(description = "공지사항 게시글 제목", example = "25.11월 업데이트 내용", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        String title,

        @Schema(description = "공지사항 게시글 내용", example = "상담 기능이 업데이트 됐습니다.")
        String description,

        @Schema(description = "삭제할 기존 이미지 ID 목록", example = "[4, 5]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<Long> deleteImageIds
) {
    public static AnnouncementUpdateRequest of(
            String title,
            String description,
            List<Long> deleteImageIds
    ) {
        return new AnnouncementUpdateRequest(title, description, deleteImageIds);
    }
}
