package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class AccountDeactivatedException extends BadRequestException {
    public AccountDeactivatedException() {
        super("탈퇴한 계정입니다.");
    }
}
