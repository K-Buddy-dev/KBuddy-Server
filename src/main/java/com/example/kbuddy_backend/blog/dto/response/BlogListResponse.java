package com.example.kbuddy_backend.blog.dto.response;

import java.util.List;

public record BlogListResponse(
    List<BlogResponse> blogs,
    boolean hasNext,
    Long nextCursor
) {
    public static BlogListResponse of(List<BlogResponse> blogs, boolean hasNext, Long nextCursor) {
        return new BlogListResponse(blogs, hasNext, nextCursor);
    }
} 