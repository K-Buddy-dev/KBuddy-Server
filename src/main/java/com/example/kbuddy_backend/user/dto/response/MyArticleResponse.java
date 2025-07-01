package com.example.kbuddy_backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MyArticleResponse(
        Long id,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        String postType, // "BLOG" 또는 "QNA"
        List<Integer> categoryId,
        String title,
        String description,
        int viewCount,
        int heartCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String thumbnailImageUrl,
        boolean isHearted,
        boolean isBookmarked
) {
    public static MyArticleResponse of(
            Long id,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            String postType,
            List<Integer> categoryId,
            String title,
            String description,
            int viewCount,
            int heartCount,
            int commentCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            String thumbnailImageUrl,
            boolean isHearted,
            boolean isBookmarked
    ) {
        return new MyArticleResponse(
                id, writerUuid, writerName, writerProfileImageUrl, postType, categoryId, title, description,
                viewCount, heartCount, commentCount, createdAt, modifiedAt, thumbnailImageUrl, isHearted, isBookmarked
        );
    }
}