package com.example.kbuddy_backend.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Blog 게시글 업데이트 요청")
public record BlogUpdateRequest(
        @Schema(description = "게시글 제목", example = "블로그 게시글 제목1", maxLength = 100)
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @Schema(description = "게시글 내용", example = "블로그 게시글 내용1")
        @NotBlank(message = "내용은 필수입니다.")
        String description,

        @Schema(hidden = true)
        List<String> hashtags,

        @Schema(description = "카테고리 ID", example = "2")
        List<Integer> categoryId,

        @Schema(description = "삭제할 기존 이미지 파일 ID 목록")
        List<Long> deleteImageIds

) {

    public static BlogUpdateRequest of(
            String title,
            String description,
            List<String> hashtags,
            List<Integer> categoryId,
            List<Long> deleteImageIds) {
        return new BlogUpdateRequest(title, description, hashtags, categoryId, deleteImageIds);
    }
}