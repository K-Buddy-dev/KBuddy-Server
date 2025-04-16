package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BlogImageRequest(
        @NotEmpty(message = "이미지 목록은 비어있을 수 없습니다.")
        List<String> images) {
}
