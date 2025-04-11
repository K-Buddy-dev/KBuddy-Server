package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BlogSaveRequest(
        @NotNull(message = "카테고리 ID는 필수입니다.")
        List<Integer> categoryId,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String description,

        @Schema(hidden = true)
        List<String> hashtags) {

    public static BlogSaveRequest of(String title, String description, List<String> hashtags, List<Integer> categoryId) {
        return new BlogSaveRequest(categoryId, title, description, hashtags);
    }
}
