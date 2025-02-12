package com.example.kbuddy_backend.blog.dto.response;

import com.example.kbuddy_backend.blog.entity.Category;
import java.time.LocalDateTime;
import java.util.List;

public record BlogResponse(
    Long id,
    String title,
    String content,
    String writer,
    Category category,
    List<String> imageUrls,
    int heartCount,
    int commentCount,
    int viewCount,
    List<BlogCommentResponse> comments,
    LocalDateTime createdDate
) {
    public static BlogResponse of(Long id, String title, String content, String writer,
                                Category category, List<String> imageUrls, int heartCount, 
                                int commentCount, int viewCount, List<BlogCommentResponse> comments, 
                                LocalDateTime createdDate) {
        return new BlogResponse(id, title, content, writer, category, imageUrls, 
                              heartCount, commentCount, viewCount, comments, createdDate);
    }
} 