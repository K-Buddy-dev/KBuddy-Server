package com.example.kbuddy_backend.qna.dto.request;

import java.util.List;

public record QnaUpdateRequest(String title, String description, List<String> hashtags, Long categoryId) {
    public static QnaUpdateRequest of(String title, String description, List<String> hashtags, Long categoryId) {
        return new QnaUpdateRequest(title, description, hashtags, categoryId);
    }
}
