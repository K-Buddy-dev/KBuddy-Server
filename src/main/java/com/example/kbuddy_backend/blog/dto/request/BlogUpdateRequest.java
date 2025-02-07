package com.example.kbuddy_backend.blog.dto.request;

public record BlogUpdateRequest(String title, String content) {
    public static BlogUpdateRequest of(String title, String content) {
        return new BlogUpdateRequest(title, content);
    }
} 