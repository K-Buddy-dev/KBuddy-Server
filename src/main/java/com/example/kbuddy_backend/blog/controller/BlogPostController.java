package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCreateRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogCreateResponse;
import com.example.kbuddy_backend.blog.service.BlogPostService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/blogs")
public class BlogPostController {

    private final BlogPostService blogService;

    @PostMapping
    public ResponseEntity<BlogCreateResponse> createBlog(@RequestBody BlogCreateRequest request, @CurrentUser User user) {
        BlogCreateResponse blogCreateRes = blogService.createBlog(request,user);
        return ResponseEntity.created(null).body(blogCreateRes);
    }
}
