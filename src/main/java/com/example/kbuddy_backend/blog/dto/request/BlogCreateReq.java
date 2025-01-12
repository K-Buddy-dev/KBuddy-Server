package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BlogCreateReq(@NotBlank String title, @NotBlank String content) {
}
