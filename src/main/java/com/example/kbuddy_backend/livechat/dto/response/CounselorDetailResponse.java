package com.example.kbuddy_backend.livechat.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CounselorDetailResponse(
        String counselorId,
        String name,
        String title,
        String detail,
        String intro,
        String professionalBackground,
        String coverImageUrl,
        String proofFileUrl,
        List<String> photoUrls,
        List<String> categories,
        BigDecimal ratingAvg,
        int reviewCount,
        int regularPrice,
        int sessionMinutes,
        String timezone,
        String profileImageUrl,
        PromotionInfo promotion,
        List<RecentReview> recentReviews) {

    public record PromotionInfo(
            int promotionalPrice,
            int promotionSessionMinutes,
            String startDate,
            String endDate,
            boolean isActive) {
    }

    public record RecentReview(
            Long reviewId,
            String customerName,
            int rating,
            String comment,
            String createdAt) {
    }
}
