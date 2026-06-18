package com.example.kbuddy_backend.blog.dto.response;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.blog.constant.BlogType;

import java.time.LocalDateTime;
import java.util.List;

public record BlogPaginationResponse(
        Long id,
        BlogType type,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        List<Integer> categoryId,
        String title,
        String description,
        int viewCount,
        int heartCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        BlogStatus status,
        boolean isBookmarked,
        boolean isHearted,
        String thumbnailImageUrl
) {
    public static BlogPaginationResponse of(
            Long id,
            BlogType type,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            List<Integer> categoryId,
            String title,
            String description,
            int viewCount,
            int heartCount,
            int commentCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            BlogStatus status,
            boolean isBookmarked,
            boolean isHearted,
            String thumbnailImageUrl
    ) {
        return new BlogPaginationResponse(
                id,
                type,
                writerUuid,
                writerName,
                writerProfileImageUrl,
                categoryId,
                title,
                description,
                viewCount,
                heartCount,
                commentCount,
                createdAt,
                modifiedAt,
                status,
                isBookmarked,
                isHearted,
                thumbnailImageUrl
        );
    }
}
