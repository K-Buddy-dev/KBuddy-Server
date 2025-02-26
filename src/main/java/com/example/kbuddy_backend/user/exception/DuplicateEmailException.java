package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.DuplicateException;

public class DuplicateEmailException extends DuplicateException {
    public DuplicateEmailException() {
        super("이미 존재하는 이메일입니다.");
    }
}
