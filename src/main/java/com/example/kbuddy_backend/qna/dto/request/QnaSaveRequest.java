package com.example.kbuddy_backend.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import com.example.kbuddy_backend.qna.constant.QnaStatus;

public record QnaSaveRequest(

        @Schema(description = "카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "카테고리 ID는 필수입니다")
        int categoryId,

        @Schema(description = "게시글 제목", example = "Qna 게시글 제목1", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        String title,

        @Schema(description = "게시글 내용", example = "Qna 게시글 내용1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용은 필수입니다")
        String description,

        @Schema(hidden =  true, description = "해시태그 목록", example = "[\"java\", \"spring\"]")
        List<String> hashtags,

        @Schema(description = "게시물 상태 (DRAFT or PUBLISHED), 미입력 시 DRAFT", example = "DRAFT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "게시물 상태는 필수입니다")
        QnaStatus status
) {
    public static QnaSaveRequest of(int categoryId, String title, String description, List<String> hashtags, QnaStatus status) {
        return new QnaSaveRequest(categoryId, title, description, hashtags, status);
    }
}
