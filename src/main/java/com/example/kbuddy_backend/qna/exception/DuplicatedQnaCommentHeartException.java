package com.example.kbuddy_backend.qna.exception;

public class DuplicatedQnaCommentHeartException extends RuntimeException {
    public DuplicatedQnaCommentHeartException() {
        super("이미 좋아요를 누른 댓글입니다.");
    }
}
