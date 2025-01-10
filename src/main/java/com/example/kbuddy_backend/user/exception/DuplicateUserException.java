package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class DuplicateUserException extends BadRequestException {
    public DuplicateUserException() {
        super("이미 존재하는 사용자입니다.");
    }
}
