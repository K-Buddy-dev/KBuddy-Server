package com.example.kbuddy_backend.blog.repository;

public interface BlogReportSummary {

    Long getBlogId();

    String getBlogTitle();

    Long getWriterId();

    String getWriterUsername();

    String getWriterEmail();

    Long getReportCount();
}


