package com.example.kbuddy_backend.blog.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class BlogCommentNotFoundException extends NotFoundException {
    public BlogCommentNotFoundException() {
        super("댓글을 찾을 수 없습니다.");
    }
} 