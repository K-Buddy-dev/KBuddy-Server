package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Blog 게시글 업데이트 요청")
public record BlogUpdateRequest(
        @Schema(description = "게시글 제목", example = "블로그 게시글 제목1", maxLength = 100, requiredMode = RequiredMode.REQUIRED)
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @Schema(description = "게시글 내용", example = "블로그 게시글 내용1", requiredMode = RequiredMode.REQUIRED)
        String description,

        @Schema(hidden = true)
        List<String> hashtags,

        @Schema(description = "카테고리 ID", example = "2", requiredMode = RequiredMode.REQUIRED)
        List<Integer> categoryId,

        @Schema(description = "변경할 게시물 상태 (DRAFT or PUBLISHED)", example = "DRAFT", requiredMode = RequiredMode.NOT_REQUIRED)
        BlogStatus status,

        @Schema(description = "삭제할 기존 이미지 파일 ID 목록", example = "[1, 2, 3]", requiredMode = RequiredMode.NOT_REQUIRED)
        List<Long> deleteImageIds

) {

    public static BlogUpdateRequest of(
            String title,
            String description,
            List<String> hashtags,
            List<Integer> categoryId,
            List<Long> deleteImageIds,
            BlogStatus status) {
        return new BlogUpdateRequest(title, description, hashtags, categoryId, status, deleteImageIds);
    }
}