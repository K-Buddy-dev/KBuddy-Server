package com.example.kbuddy_backend.announcement.dto.response;

import com.example.kbuddy_backend.common.dto.ImageFileDto;

import java.time.LocalDateTime;
import java.util.List;

public record AnnouncementResponse(
    Long id,
    String writerUuid,
    String writerName,
    String writerProfileImageUrl,
    String title,
    String description,
    int viewCount,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<ImageFileDto> images
) {
    public static AnnouncementResponse of(
            Long id,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            String title,
            String description,
            int viewCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            List<ImageFileDto>images
) {
    return new AnnouncementResponse(
            id,
            writerUuid,
            writerName,
            writerProfileImageUrl,
            title,
            description,
            viewCount,
            createdAt,
            modifiedAt,
            images
    );
    }
}