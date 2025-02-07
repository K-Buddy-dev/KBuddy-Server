package com.example.kbuddy_backend.blog.dto.response;

import java.time.LocalDateTime;

public record BlogResponse(
    Long id,
    String title,
    String content,
    String writer,
    int heartCount,
    int commentCount,
    int viewCount,
    LocalDateTime createdDate
) {
    public static BlogResponse of(Long id, String title, String content, String writer,
                                int heartCount, int commentCount, int viewCount, LocalDateTime createdDate) {
        return new BlogResponse(id, title, content, writer, heartCount, commentCount, viewCount, createdDate);
    }
} 