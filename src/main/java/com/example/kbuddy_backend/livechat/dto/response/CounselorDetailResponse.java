package com.example.kbuddy_backend.livechat.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CounselorDetailResponse(
        String counselorId,
        String name,
        String intro,
        BigDecimal ratingAvg,
        int reviewCount,
        int slotRate,
        String timezone,
        String profileImageUrl,
        List<RecentReview> recentReviews) {
    public record RecentReview(
            Long reviewId,
            String customerName,
            int rating,
            String comment,
            String createdAt) {
    }
}
