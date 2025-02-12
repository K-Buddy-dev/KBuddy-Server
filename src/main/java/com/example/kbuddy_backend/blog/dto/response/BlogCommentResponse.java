package com.example.kbuddy_backend.blog.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BlogCommentResponse(
    Long id,
    String content,
    String writer,
    int heartCount,
    boolean isReply,
    Long parentId,
    List<BlogCommentResponse> replies,
    LocalDateTime createdDate
) {
    public static BlogCommentResponse of(Long id, String content, String writer,
                                       int heartCount, boolean isReply, Long parentId,
                                       List<BlogCommentResponse> replies, LocalDateTime createdDate) {
        return new BlogCommentResponse(id, content, writer, heartCount, isReply, parentId, replies, createdDate);
    }
} 