package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryListResponse(
        List<InquiryItem> inquiries,
        long totalCount) {

    public record InquiryItem(
            Long inquiryId,
            String title,
            String writerName,
            boolean isSecret,
            LocalDateTime createdAt) {
    }
}
