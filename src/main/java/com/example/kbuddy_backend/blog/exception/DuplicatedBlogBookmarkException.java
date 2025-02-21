package com.example.kbuddy_backend.blog.exception;

public class DuplicatedBlogBookmarkException extends RuntimeException {
    public DuplicatedBlogBookmarkException() {
        super("이미 북마크에 추가된 블로그입니다.");
    }
} 