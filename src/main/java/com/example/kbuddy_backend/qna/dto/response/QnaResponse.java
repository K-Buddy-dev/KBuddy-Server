package com.example.kbuddy_backend.qna.dto.response;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record QnaResponse(
        Long id,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        int categoryId,
        String title,
        String description,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<ImageFileDto> images,
        List<QnaCommentResponse> comments,
        int heartCount,
        int commentCount,
        boolean isBookmarked,
        boolean isHearted,
        QnaStatus status
) {
    public static QnaResponse of(
            Long id,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            int categoryId,
            String title,
            String description,
            int viewCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            List<ImageFileDto> images,
            List<QnaCommentResponse> comments,
            int heartCount,
            int commentCount,
            boolean isBookmarked,
            boolean isHearted,
            QnaStatus status
    ) {
        return new QnaResponse(
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
