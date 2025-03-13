package com.example.kbuddy_backend.qna.dto.response;

import java.util.List;

public record AllQnaResponse(Long nextId, List<QnaPaginationResponse> results) {
    public static AllQnaResponse of(Long nextId, List<QnaPaginationResponse> results) {
        return new AllQnaResponse(nextId, results);
    }
}
