package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogCommentSaveRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다.")
        String content,
        Long parentId
) {
}