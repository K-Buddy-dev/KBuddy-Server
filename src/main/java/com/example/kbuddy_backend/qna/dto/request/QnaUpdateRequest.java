package com.example.kbuddy_backend.qna.dto.request;

import com.example.kbuddy_backend.qna.constant.QnaStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Q&A 게시글 업데이트 요청")
public record QnaUpdateRequest(
        @Schema(description = "게시글 제목", example = "수정된 사이버베인 작가의 다음 작품은 언제 나오나요?", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        String title,

        @Schema(description = "게시글 내용", example = "사이버베인 작가의 최근 인터뷰에서 다음 작품에 대한 예상 일정이 있었는지 알고 싶습니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String description,

        @Schema(hidden =  true, description = "수정할 해시태그 목록", example = "[\"update\", \"release\"]")
        List<String> hashtags,

        @Schema(description = "카테고리 ID", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer categoryId,

        @Schema(description = "변경할 게시물 상태 (DRAFT or PUBLISHED)", example = "DRAFT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        QnaStatus status,

        @Schema(description = "삭제할 기존 이미지 ID 목록", example = "[4, 5]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<Long> deleteImageIds
) {
}
