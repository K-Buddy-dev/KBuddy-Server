package com.example.kbuddy_backend.blog.exception;

public class BlogBookmarkNotFoundException extends RuntimeException {
    public BlogBookmarkNotFoundException() {
        super("북마크를 찾을 수 없습니다.");
    }
} 