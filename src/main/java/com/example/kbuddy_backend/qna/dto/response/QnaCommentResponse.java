package com.example.kbuddy_backend.qna.dto.response;

import java.time.LocalDateTime;
import java.util.List;

//todo:UUID로 변경
public record QnaCommentResponse(
        Long id,
        Long qnaId,
        String writerUuid,
        String writerName,
        String writerProfileImageUrl,
        String description,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<QnaCommentResponse> replies,
        int heartCount,
        boolean isHearted
) {
    public static QnaCommentResponse of(
            Long id,
            Long qnaId,
            String writerUuid,
            String writerName,
            String writerProfileImageUrl,
            String description,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            List<QnaCommentResponse> replies,
            int heartCount,
            boolean isHearted
    ) {
        return new QnaCommentResponse(
                id,
                qnaId,
                writerUuid,
                writerName,
                writerProfileImageUrl,
                description,
                createdAt,
                modifiedAt,
                replies,
                heartCount,
                isHearted
        );
    }
}
