package com.example.kbuddy_backend.blog.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class BlogNotFoundException extends NotFoundException {
    public BlogNotFoundException() {
        super("블로그를 찾을 수 없습니다.");
    }
} 