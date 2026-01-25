package com.example.kbuddy_backend.livechat.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CounselorListResponse(
        List<CounselorSummary> content,
        long totalElements) {
    public record CounselorSummary(
            String counselorId,
            String name,
            String intro,
            String specialty,
            BigDecimal ratingAvg,
            int reviewCount,
            int hourlyRate,
            String profileImageUrl) {
    }
}
