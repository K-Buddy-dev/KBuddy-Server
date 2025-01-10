package com.example.kbuddy_backend.auth.exception;

import com.example.kbuddy_backend.common.exception.UnauthorizedException;

public class TokenNotFoundException extends UnauthorizedException {
    public TokenNotFoundException() {
        super("토큰이 존재하지 않습니다.");
    }
}
