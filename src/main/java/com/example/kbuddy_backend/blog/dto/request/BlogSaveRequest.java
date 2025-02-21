package com.example.kbuddy_backend.blog.dto.request;

import com.example.kbuddy_backend.blog.entity.Category;
import java.util.List;

public record BlogSaveRequest(
    String title,
    String content,
    Category category,
    List<String> imageUrls
) {
    public static BlogSaveRequest of(String title, String content, Category category, List<String> imageUrls) {
        return new BlogSaveRequest(title, content, category, imageUrls);
    }
} 