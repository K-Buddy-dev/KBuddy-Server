package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class InvalidPasswordException extends BadRequestException {
    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}
