package com.example.kbuddy_backend.blog.dto.request;

public record BlogCommentRequest(String content) {
    public static BlogCommentRequest of(String content) {
        return new BlogCommentRequest(content);
    }
} 