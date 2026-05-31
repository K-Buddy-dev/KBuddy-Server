package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewListResponse(
        List<ReviewItem> reviews,
        long totalCount) {

    public record ReviewItem(
            Long reviewId,
            String customerName,
            int rating,
            String comment,
            LocalDateTime createdAt) {
    }
}
