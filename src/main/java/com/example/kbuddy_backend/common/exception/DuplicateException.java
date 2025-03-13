package com.example.kbuddy_backend.common.exception;

public class DuplicateException extends RuntimeException{
        public DuplicateException(String message) {
            super(message);
        }
}
