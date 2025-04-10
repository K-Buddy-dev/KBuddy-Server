package com.example.kbuddy_backend.blog.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BlogPaginationResponse(Long id, Long writerId, List<Integer> categoryId, String title, String description,
                                     int viewCount, int heartCount,
                                     int commentCount, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    public static BlogPaginationResponse of(Long id, Long writerId, List<Integer> categoryId, String title, String description,
                                            int viewCount, int heartCount,
                                            int commentCount, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new BlogPaginationResponse(id, writerId, categoryId, title, description, viewCount, heartCount,
                commentCount, createdAt, modifiedAt);
    }
}
