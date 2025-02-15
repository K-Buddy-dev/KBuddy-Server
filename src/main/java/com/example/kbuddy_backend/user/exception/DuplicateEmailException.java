package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class DuplicateEmailException extends BadRequestException {
    public DuplicateEmailException() {
        super("이미 존재하는 이메일입니다.");
    }
}
