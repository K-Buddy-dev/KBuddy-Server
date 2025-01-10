package com.example.kbuddy_backend.qna.exception;

public class DuplicatedQnaHeartException extends RuntimeException {
    public DuplicatedQnaHeartException() {
        super("이미 좋아요를 누른 질문 게시글입니다.");
    }
}
