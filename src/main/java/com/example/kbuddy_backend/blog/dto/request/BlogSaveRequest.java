package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BlogSaveRequest(

        @Schema(description = "카테고리 ID", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "카테고리 ID는 필수입니다.")
        List<Integer> categoryId,

        @Schema(description = "게시글 제목", example = "블로그 게시글 제목", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "제목 필드는 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @Schema(description = "게시글 내용", example = "블로그 게시글 내용", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "내용 필드는 필수입니다.")
        String description,

        @Schema(hidden = true)
        List<String> hashtags,

        @Schema(description = "게시물 상태 (DRAFT or PUBLISHED), 미입력 시 DRAFT", example = "DRAFT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        BlogStatus status
) {
    public static BlogSaveRequest of(String title, String description, List<String> hashtags, List<Integer> categoryId, BlogStatus status) {
        return new BlogSaveRequest(categoryId, title, description, hashtags, status);
    }
}
