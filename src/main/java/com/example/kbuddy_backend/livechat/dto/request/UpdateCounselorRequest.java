package com.example.kbuddy_backend.livechat.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record UpdateCounselorRequest(
        @Size(max = 100) String title,
        String detail,
        String intro,
        String professionalBackground,
        @Size(min = 1, max = 5) List<String> categories,
        @Positive Integer regularPrice,
        @Min(15) @Max(120) Integer sessionMinutes,
        String timezone,
        Integer promotionalPrice,
        Integer promotionSessionMinutes,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate promotionStartDate,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate promotionEndDate,
        List<String> existingPhotoUrls,
        List<SlotRequest> slots) {
}
