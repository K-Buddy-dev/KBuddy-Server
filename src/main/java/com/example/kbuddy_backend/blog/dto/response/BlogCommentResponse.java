package com.example.kbuddy_backend.blog.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BlogCommentResponse(Long id, Long blogId, Long writerId, String description,
                                 LocalDateTime createdAt, LocalDateTime modifiedAt) {
    public static BlogCommentResponse of(Long id, Long blogId, Long writerId, String description,
                                                                                    LocalDateTime createdAt, LocalDateTime modifiedAt
    ) {
        return new BlogCommentResponse(id, blogId, writerId, description, createdAt, modifiedAt);
    }
}