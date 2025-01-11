package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogCreateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 100, message = "제목은 1-100자 사이여야 합니다.")
    String title,
    
    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 10, message = "내용은 최소 10자 이상이어야 합니다.")
    String content
) { }