package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class UserBlockException extends BadRequestException {
    public UserBlockException(String message) {
        super(message);
    }
} 