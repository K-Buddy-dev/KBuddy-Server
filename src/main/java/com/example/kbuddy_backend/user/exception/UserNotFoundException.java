package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("User not found.");
    }
}
