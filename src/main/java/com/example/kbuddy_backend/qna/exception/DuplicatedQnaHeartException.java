package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class DuplicatedQnaHeartException extends BadRequestException {
    public DuplicatedQnaHeartException() {
        super("이미 좋아요를 누른 질문 게시글입니다.");
    }
}
