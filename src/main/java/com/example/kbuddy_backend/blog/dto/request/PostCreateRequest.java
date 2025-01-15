package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record PostCreateRequest(
        @NotNull String title,
        @NotNull String description,
        @NotNull Integer categoryId
) {
}