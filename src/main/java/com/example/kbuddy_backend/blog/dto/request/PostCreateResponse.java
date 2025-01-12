package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class PostCreateResponse {
    @JsonProperty
    private final Long id;
    @JsonProperty
    private final String title;
    @JsonProperty
    private final String description;
    @JsonProperty
    private final Integer categoryId;
    @JsonProperty
    private final LocalDateTime createdAt;

    public PostCreateResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.categoryId = post.getCategoryId();
        this.createdAt = post.getCreatedDate();
    }
}