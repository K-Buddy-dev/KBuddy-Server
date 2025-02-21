package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.entity.Category;
import java.util.List;

public record BlogUpdateRequest(
    String title,
    String content,
    Category category,
    List<String> imageUrls
) {
    public static BlogUpdateRequest of(String title, String content, Category category, List<String> imageUrls) {
        return new BlogUpdateRequest(title, content, category, imageUrls);
    }
} 