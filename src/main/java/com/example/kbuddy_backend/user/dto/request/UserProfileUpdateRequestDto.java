package com.example.kbuddy_backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 프로필 수정 요청 DTO")
public record UserProfileUpdateRequestDto(
        @Schema(description = "수정할 자기소개 (null 가능)", example = "새로운 자기소개입니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String bio
) {
} 