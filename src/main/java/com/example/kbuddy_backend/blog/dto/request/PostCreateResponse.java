package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record PostCreateResponse(
        @JsonProperty Long id,
        @JsonProperty String title,
        @JsonProperty String description,
        @JsonProperty Integer categoryId,
        @JsonProperty LocalDateTime createdAt
) {
    public PostCreateResponse(Post post) {
        this(post.getId(), post.getTitle(), post.getDescription(), post.getCategoryId(), post.getCreatedDate());
    }
}