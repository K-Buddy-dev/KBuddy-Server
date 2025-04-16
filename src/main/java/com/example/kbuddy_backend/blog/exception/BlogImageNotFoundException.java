package com.example.kbuddy_backend.blog.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class BlogImageNotFoundException extends NotFoundException {
    public BlogImageNotFoundException() {
        super("Blog 이미지를 찾을 수 없습니다.");
    }
}
