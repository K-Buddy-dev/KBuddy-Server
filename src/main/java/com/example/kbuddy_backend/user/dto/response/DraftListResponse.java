package com.example.kbuddy_backend.user.dto.response;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import java.time.LocalDateTime;
import java.util.List;

public record DraftListResponse(Long id, Long writerId, Object categoryId, String title, String description, List<ImageFileDto> images,
                       LocalDateTime createdAt, LocalDateTime modifiedAt,
                                String status) {
    public static DraftListResponse of(Long id, Long writerId, Object categoryId, String title, String description, List<ImageFileDto> images,
                                                                                      LocalDateTime createdAt, LocalDateTime modifiedAt,
                                                                                       String status) {
        return new DraftListResponse(id, writerId, categoryId, title, description, images, createdAt, modifiedAt, status);
    }

}
