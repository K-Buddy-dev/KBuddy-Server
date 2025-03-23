package com.example.kbuddy_backend.blog.exception;

public class DuplicatedBlogCommentHeartException extends RuntimeException{
    public DuplicatedBlogCommentHeartException() { super("이미 좋아요를 누른 댓글입니다.");}
}
