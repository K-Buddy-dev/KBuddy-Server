package com.example.kbuddy_backend.qna.dto.response;

public record QnaResponse(Long id, String title, String content, String writer, int heartCount, int commentCount,int viewCount) {
    public static QnaResponse of(Long id, String title, String content, String writer, int heartCount,
                                 int commentCount,int viewCount
    ) {
        return new QnaResponse(id, title, content, writer, heartCount, commentCount,viewCount);
    }
}
