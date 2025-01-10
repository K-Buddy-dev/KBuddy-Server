package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class QnaCommentNotFoundException extends NotFoundException {
    public QnaCommentNotFoundException() {
        super("댓글을 찾을 수 없습니다.");
    }
}
