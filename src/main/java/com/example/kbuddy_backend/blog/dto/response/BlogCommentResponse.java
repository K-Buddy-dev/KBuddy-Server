package com.example.kbuddy_backend.blog.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BlogCommentResponse(
        Long id,
        Long blogId,
        Long writerId,
        String description,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<BlogCommentResponse> replies,
        int heartCount,
        boolean isHearted
) {
    public static BlogCommentResponse of(Long id, Long blogId, Long writerId, String description,
                                       LocalDateTime createdAt, LocalDateTime modifiedAt,
                                       List<BlogCommentResponse> replies,
                                       int heartCount,
                                       boolean isHearted) {
        return new BlogCommentResponse(id, blogId, writerId, description, createdAt, modifiedAt, replies, heartCount, isHearted);
    }
}