package com.example.kbuddy_backend.blog.dto.response;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.common.dto.ImageFileDto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogResponse(
        Long id,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        List<Integer> categoryId,
        String title,
        String description,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<ImageFileDto> images,
        List<BlogCommentResponse> comments,
        int heartCount,
        int commentCount,
        boolean isBookmarked,
        boolean isHearted,
        BlogStatus status
) {
    public static BlogResponse of(
            Long id,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            List<Integer> categoryId,
            String title,
            String description,
            int viewCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            List<ImageFileDto> images,
            List<BlogCommentResponse> comments,
            int heartCount,
            int commentCount,
            boolean isBookmarked,
            boolean isHearted,
            BlogStatus status
    ) {
        return new BlogResponse(
                id,
                writerUuid,
                writerName,
                writerProfileImageUrl,
                categoryId,
                title,
                description,
                viewCount,
                createdAt,
                modifiedAt,
                images,
                comments,
                heartCount,
                commentCount,
                isBookmarked,
                isHearted,
                status
        );
    }
}