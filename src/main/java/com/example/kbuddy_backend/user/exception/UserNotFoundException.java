package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }
}
