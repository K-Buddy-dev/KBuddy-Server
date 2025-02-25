package com.example.kbuddy_backend.common.exception;

public  class InvalidDateFormatException extends BadRequestException {
    public InvalidDateFormatException() {
            super("Invalid Date Format");
        }
    }

