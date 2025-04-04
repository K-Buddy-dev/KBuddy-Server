package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import java.util.List;

public record BlogSaveRequest(Long categoryId, String title, String description, List<String> hashtags) {
    public static BlogSaveRequest of(String title, String description, List<String> hashtags, Long categoryId) {
        return new BlogSaveRequest(categoryId, title, description, hashtags);
    }
}
