package com.example.kbuddy_backend.fixtures;

import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import java.time.LocalDateTime;
import java.util.List;

public class BlogFixtures {

    public static BlogSaveRequest createBlogSaveRequest() {
        return BlogSaveRequest.of("title", "description", List.of(
                ImageFileDto.of(ImageFileType.PNG,"test_pic","test_url")), List.of("cafe","theater"), 1L);
    }

    public static BlogResponse createBlogResponse() {
        return BlogResponse.of(1L, 123L, 1L, "title", "description", 0, LocalDateTime.now(), LocalDateTime.now(), List.of(ImageFileDto.of(ImageFileType.PNG,"test_pic","test_url")), null, 0, 0);
    }
}