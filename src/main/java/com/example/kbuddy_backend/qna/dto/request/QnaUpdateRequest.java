package com.example.kbuddy_backend.qna.dto.request;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Q&A 게시글 업데이트 요청")
public record QnaUpdateRequest(
        @Schema(description = "게시글 제목", example = "수정된 사이버베인 작가의 다음 작품은 언제 나오나요?", maxLength = 100)
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        String title,

        @Schema(description = "게시글 내용", example = "사이버베인 작가의 최근 인터뷰에서 다음 작품에 대한 예상 일정이 있었는지 알고 싶습니다.")
        String description,

        @Schema(hidden = true)
        List<String> hashtags,

        @Schema(description = "카테고리 ID", example = "2")
        Integer categoryId,

        @Schema(description = "삭제할 기존 이미지 파일 ID 목록")
        List<Long> deleteImageIds

) {

    public static QnaUpdateRequest of(
            String title,
            String description,
            List<String> hashtags,
            Integer categoryId,
            List<Long> deleteImageIds
    ) {
        return new QnaUpdateRequest(title, description, hashtags, categoryId, deleteImageIds);
    }
}