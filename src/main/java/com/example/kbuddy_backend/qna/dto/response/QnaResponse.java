package com.example.kbuddy_backend.qna.dto.response;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import java.time.LocalDateTime;
import java.util.List;

public record QnaResponse(Long id, Long writerId, Long categoryId, String title, String description, int viewCount, LocalDateTime createdAt, LocalDateTime modifiedAt,
                          List<ImageFileDto> images, List<QnaCommentResponse> comments, int heartCount, int commentCount) {
    public static QnaResponse of(Long id, Long writerId, Long categoryId, String title, String description, int viewCount, LocalDateTime createdAt, LocalDateTime modifiedAt,
                                 List<ImageFileDto> images, List<QnaCommentResponse> comments, int heartCount, int commentCount) {
        return new QnaResponse(id, writerId, categoryId, title, description, viewCount, createdAt, modifiedAt, images, comments, heartCount, commentCount);
    }
}
