package com.example.kbuddy_backend.qna.dto.response;

import java.time.LocalDateTime;

//todo:UUID로 변경
public record QnaCommentResponse(Long id, Long qnaId, Long writerId, String description,
                                 LocalDateTime createdAt, LocalDateTime modifiedAt) {
    public static QnaCommentResponse of(Long id, Long qnaId, Long writerId, String description,
                                        LocalDateTime createdAt, LocalDateTime modifiedAt
    ) {
        return new QnaCommentResponse(id, qnaId, writerId, description, createdAt, modifiedAt);
    }
}
