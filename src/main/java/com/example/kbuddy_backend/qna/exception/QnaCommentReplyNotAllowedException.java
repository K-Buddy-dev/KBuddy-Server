package com.example.kbuddy_backend.qna.exception;

public class QnaCommentReplyNotAllowedException extends IllegalArgumentException {
    public QnaCommentReplyNotAllowedException(String message) {
        super(message);
    }
}
