package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BlogCreateRequest(@NotBlank String title, @NotBlank String content) {
}
