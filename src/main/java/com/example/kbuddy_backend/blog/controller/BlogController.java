package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCreateReq;
import com.example.kbuddy_backend.blog.dto.response.BlogCreateRes;
import com.example.kbuddy_backend.blog.service.BlogService;
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
public class BlogController {

    private final BlogService blogService;


    @PostMapping
    public ResponseEntity<BlogCreateRes> createBlog(@RequestBody BlogCreateReq request, @CurrentUser User user) {
        BlogCreateRes blogCreateRes = blogService.createBlog(request,user);
        return ResponseEntity.created(null).body(blogCreateRes);
    }
}
