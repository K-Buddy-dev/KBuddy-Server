package com.example.kbuddy_backend.fixtures;

import com.example.kbuddy_backend.user.dto.response.DefaultResponse;

public class DefaultMessageFixtures {
    
    public static DefaultResponse createQnaDefaultResponse() {
        return DefaultResponse.of(true, "게시글 작성 성공");
    }
}
