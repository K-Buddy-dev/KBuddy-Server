package com.example.kbuddy_backend.blog.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class BlogBookmarkNotFoundException extends NotFoundException {
    public BlogBookmarkNotFoundException() {
        super("블로그 북마크를 찾을 수 없습니다.");
    }
} 