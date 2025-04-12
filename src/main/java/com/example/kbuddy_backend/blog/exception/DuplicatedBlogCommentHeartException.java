package com.example.kbuddy_backend.blog.exception;

import com.example.kbuddy_backend.common.exception.BadRequestException;

public class DuplicatedBlogCommentHeartException extends BadRequestException {
    public DuplicatedBlogCommentHeartException() { super("이미 좋아요를 누른 댓글입니다.");}
}
