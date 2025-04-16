package com.example.kbuddy_backend.blog.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class DuplicatedBlogHeartException extends BadRequestException {
    public DuplicatedBlogHeartException() {
        super("이미 좋아요를 누른 블로그입니다.");
    }
} 