package com.example.kbuddy_backend.qna.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class QnaBookmarkNotFoundException extends NotFoundException {
    public QnaBookmarkNotFoundException() {
        super("QnA 북마크를 찾을 수 없습니다.");
    }

}
