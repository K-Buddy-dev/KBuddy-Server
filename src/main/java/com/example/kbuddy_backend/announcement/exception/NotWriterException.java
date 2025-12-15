package com.example.kbuddy_backend.announcement.exception;

public class NotWriterException extends IllegalArgumentException{
    public NotWriterException() {
        super("작성자가 아닙니다.");
    }
}
