package com.example.kbuddy_backend.blog.dto.response;

import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import java.util.List;

public record AllBlogResponse(Long nextId, List<BlogPaginationResponse> results) {
    public static AllBlogResponse of(Long nextId, List<BlogPaginationResponse> results) {
        return new AllBlogResponse(nextId, results);
    }
}
