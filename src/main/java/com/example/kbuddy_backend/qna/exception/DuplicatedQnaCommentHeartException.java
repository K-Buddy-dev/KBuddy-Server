package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class DuplicatedQnaCommentHeartException extends BadRequestException {
    public DuplicatedQnaCommentHeartException() {
        super("이미 좋아요를 누른 댓글입니다.");
    }
}
