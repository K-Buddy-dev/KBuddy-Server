package com.example.kbuddy_backend.blog.dto.response;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import java.time.LocalDateTime;
import java.util.List;

public record BlogResponse(Long id, Long writerId, List<Integer> categoryId, String title, String description, int viewCount, LocalDateTime createdAt, LocalDateTime modifiedAt,
                          List<ImageFileDto> images, List<BlogCommentResponse> comments, int heartCount, int commentCount) {
    public static BlogResponse of(Long id, Long writerId, List<Integer> categoryId, String title, String description, int viewCount, LocalDateTime createdAt, LocalDateTime modifiedAt,
                                  List<ImageFileDto> images, List<BlogCommentResponse> comments, int heartCount, int commentCount) {
        return new BlogResponse(id, writerId, categoryId, title, description, viewCount, createdAt, modifiedAt, images, comments, heartCount, commentCount);
    }
}