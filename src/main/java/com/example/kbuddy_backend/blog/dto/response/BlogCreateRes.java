package com.example.kbuddy_backend.blog.dto.response;

public record BlogCreateRes(Long id, Long writerId) {
    public static BlogCreateRes of(Long id, Long writerId) {
        return new BlogCreateRes(id, writerId);
    }
}
