package com.example.kbuddy_backend.livechat.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CounselorListResponse(
        List<CounselorSummary> content,
        long totalElements) {

    public record CounselorSummary(
            String counselorId,
            String name,
            String title,
            List<String> categories,
            BigDecimal ratingAvg,
            int reviewCount,
            int regularPrice,
            int sessionMinutes,
            String coverImageUrl,
            boolean hasPromotion) {
    }
}
