package com.example.kbuddy_backend.blog.dto.response;

public record BlogCreateResponse(Long id, Long writerId) {
    public static BlogCreateResponse of(Long id, Long writerId) {
        return new BlogCreateResponse(id, writerId);
    }
}
