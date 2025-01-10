package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class QnaNotFoundException extends NotFoundException {
    public QnaNotFoundException() {
        super("QnA를 찾을 수 없습니다.");
    }
}
