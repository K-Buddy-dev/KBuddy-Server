package com.example.kbuddy_backend.blog.dto.request;

public record BlogSaveRequest(String title, String content) {
    public static BlogSaveRequest of(String title, String content) {
        return new BlogSaveRequest(title, content);
    }
} 