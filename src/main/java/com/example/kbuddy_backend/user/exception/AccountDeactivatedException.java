package com.example.kbuddy_backend.user.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class AccountDeactivatedException extends BadRequestException {
    public AccountDeactivatedException() {
        super("This account has been withdrawn.");
    }
}
