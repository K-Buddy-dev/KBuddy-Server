package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BlogUpdateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String description,

        List<String> hashtags,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        int categoryId) {

    public static BlogUpdateRequest of(String title, String description, List<String> hashtags, int categoryId) {
        return new BlogUpdateRequest(title, description, hashtags, categoryId);
    }
}