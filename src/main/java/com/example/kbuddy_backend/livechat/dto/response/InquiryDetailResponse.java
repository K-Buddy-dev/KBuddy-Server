package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryDetailResponse(
        Long inquiryId,
        String title,
        String content,
        String writerName,
        boolean isSecret,
        LocalDateTime createdAt,
        List<ReplyItem> replies) {

    public record ReplyItem(
            Long replyId,
            String authorName,
            String content,
            LocalDateTime createdAt) {
    }
}
