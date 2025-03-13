package com.example.kbuddy_backend.qna.dto.response;

import java.time.LocalDateTime;

/*
* 고려사항 : 카테고리 관리
* */
public record QnaPaginationResponse(Long id, Long writerId, Long categoryId, String title, String description,
                                    int viewCount, int likeCount,
                                    int commentCount, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    public static QnaPaginationResponse of(Long id, Long writerId, Long categoryId, String title, String description,
                                           int viewCount, int heartCount,
                                           int commentCount, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new QnaPaginationResponse(id, writerId, categoryId, title, description, viewCount, heartCount,
                commentCount, createdAt, modifiedAt);
    }
}
