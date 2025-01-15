package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.PostCreateRequest;
import com.example.kbuddy_backend.blog.dto.request.PostCreateResponse;
import com.example.kbuddy_backend.blog.entity.Post;
import com.example.kbuddy_backend.blog.service.BlogService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("kbuddy/v1/blog")
public class BlogController {

    private final BlogService blogService;

    @PostMapping("/post")
    public ResponseEntity<PostCreateResponse> createPost(
            @Valid
            @RequestBody
            PostCreateRequest request,
            @CurrentUser User user
    ) {
        PostCreateResponse response = blogService.createPost(request, user);
        return ResponseEntity.ok(response);
    }
}
