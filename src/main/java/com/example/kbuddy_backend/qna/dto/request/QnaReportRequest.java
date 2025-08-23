package com.example.kbuddy_backend.qna.dto.request;

import com.example.kbuddy_backend.blog.dto.request.BlogReportRequest;

public record QnaReportRequest(String content) {
    public QnaReportRequest of(String content) { return  new QnaReportRequest(content); }
}
