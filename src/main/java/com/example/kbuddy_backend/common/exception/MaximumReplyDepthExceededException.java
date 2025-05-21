package com.example.kbuddy_backend.common.exception;

public class MaximumReplyDepthExceededException extends BadRequestException {
    public MaximumReplyDepthExceededException() {
        super("대댓글은 1단계까지만 작성 가능합니다.");
    }
} 