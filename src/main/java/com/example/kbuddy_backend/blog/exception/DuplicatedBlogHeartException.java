package com.example.kbuddy_backend.blog.exception;

public class DuplicatedBlogHeartException extends RuntimeException {
    public DuplicatedBlogHeartException() {
        super("이미 좋아요를 누른 블로그입니다.");
    }
} 