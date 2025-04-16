package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class QnaImageNotFoundException extends NotFoundException {
    public QnaImageNotFoundException() {
        super("QnA 이미지를 찾을 수 없습니다.");
    }
}
