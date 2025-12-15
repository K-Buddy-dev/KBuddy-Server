package com.example.kbuddy_backend.announcement.dto.response;

import java.time.LocalDateTime;

public record AnnouncementPaginationResponse(
        Long id,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        String title,
        String description,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String thumbnailImageUrl
) {
    public static AnnouncementPaginationResponse of(
            Long id,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            String title,
            String description,
            int viewCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            String thumbnailImageUrl
    ) {
        return new AnnouncementPaginationResponse(
                id,
                writerUuid,
                writerName,
                writerProfileImageUrl,
                title,
                description,
                viewCount,
                createdAt,
                modifiedAt,
                thumbnailImageUrl
        );
    }
}
