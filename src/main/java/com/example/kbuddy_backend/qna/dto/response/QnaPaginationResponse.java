package com.example.kbuddy_backend.qna.dto.response;

import com.example.kbuddy_backend.qna.constant.QnaStatus;
import java.time.LocalDateTime;

/*
* 고려사항 : 카테고리 관리
* */
public record QnaPaginationResponse(
        Long id,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        int categoryId,
        String title,
        String description,
        int viewCount,
        int heartCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        QnaStatus status,
        boolean isBookmarked,
        boolean isHearted,
        String thumbnailImageUrl
) {
    public static QnaPaginationResponse of(
            Long id,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            int categoryId,
            String title,
            String description,
            int viewCount,
            int heartCount,
            int commentCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            QnaStatus status,
            boolean isBookmarked,
            boolean isHearted,
            String thumbnailImageUrl
    ) {
        return new QnaPaginationResponse(
                id,
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
