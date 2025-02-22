package com.example.kbuddy_backend.user.exception;
import com.example.kbuddy_backend.common.exception.DuplicateException;

public class DuplicateUserIdException extends DuplicateException {
    public DuplicateUserIdException() {
        super("이미 존재하는 사용자 아이디입니다.");
    }
}
