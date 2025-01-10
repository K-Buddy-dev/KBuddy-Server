package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class QnaHeartNotFoundException extends NotFoundException {
    public QnaHeartNotFoundException() {
        super("QnaHeart를 찾을 수 없습니다.");
    }
}
