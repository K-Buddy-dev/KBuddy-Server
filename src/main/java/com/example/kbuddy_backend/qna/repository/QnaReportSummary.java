package com.example.kbuddy_backend.qna.repository;

public interface QnaReportSummary {

    Long getQnaId();

    String getQnaTitle();

    Long getWriterId();

    String getWriterUsername();

    String getWriterEmail();

    Long getReportCount();
}


