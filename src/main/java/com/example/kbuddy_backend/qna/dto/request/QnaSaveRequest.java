package com.example.kbuddy_backend.qna.dto.request;

import java.util.List;

public record QnaSaveRequest(Long categoryId, String title, String description, List<String> hashtags) {
    public static QnaSaveRequest of(String title, String description, List<String> hashtags, Long categoryId) {
        return new QnaSaveRequest(categoryId, title, description, hashtags);
    }
}
