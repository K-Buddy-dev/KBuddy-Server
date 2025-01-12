package com.example.kbuddy_backend.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostCreateRequest {
    @NotNull
    private String title;
    @NotNull
    private String description;
    @NotNull
    private Integer categoryId;
}
