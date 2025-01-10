package com.example.kbuddy_backend.auth.exception;

import com.example.kbuddy_backend.common.exception.UnauthorizedException;

public class TokenExpirationException extends UnauthorizedException {
    public TokenExpirationException() {
        super("만료된 토큰입니다.");
    }
}
