package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.constant.BlogCategoryEnum;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import java.util.List;

public record BlogUpdateRequest(String title, String description, List<String> hashtags, Long categoryId) {
    public static BlogUpdateRequest of(String title, String description, List<String> hashtags, Long categoryId) {
        return new BlogUpdateRequest(title, description, hashtags, categoryId);
    }
}