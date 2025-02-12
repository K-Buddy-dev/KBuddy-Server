package com.example.kbuddy_backend.blog.dto.request;

public record BlogReplyRequest(String content) {
    public static BlogReplyRequest of(String content) {
        return new BlogReplyRequest(content);
    }
} 