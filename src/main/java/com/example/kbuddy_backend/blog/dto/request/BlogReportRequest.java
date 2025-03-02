package com.example.kbuddy_backend.blog.dto.request;

public record BlogReportRequest(String content) {
    public BlogReportRequest of(String content) { return new BlogReportRequest(content); }
}
