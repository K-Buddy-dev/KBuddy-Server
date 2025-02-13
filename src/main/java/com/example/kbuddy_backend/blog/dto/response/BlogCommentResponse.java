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
    LocalDateTime createdDate,
    boolean deleted
) {
    public static BlogCommentResponse of(Long id, String content, String writer,
                                       int heartCount, boolean isReply, Long parentId,
                                       List<BlogCommentResponse> replies, LocalDateTime createdDate,
                                       boolean deleted) {
        return new BlogCommentResponse(id, content, writer, heartCount, isReply, 
                                     parentId, replies, createdDate, deleted);
    }
} 