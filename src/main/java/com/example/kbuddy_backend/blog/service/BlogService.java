package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.PostCreateRequest;
import com.example.kbuddy_backend.blog.dto.request.PostCreateResponse;
import com.example.kbuddy_backend.blog.entity.Post;
import com.example.kbuddy_backend.blog.repository.PostRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BlogService {

    private final PostRepository postRepository;

    public PostCreateResponse createPost(
            PostCreateRequest request,
            User user
    ) {
        Post post = Post.builder()
                .title(request.title())
                .description(request.description())
                .categoryId(request.categoryId())
                .user(user)
                .build();

        postRepository.save(post);

        return new PostCreateResponse(post);
    }
}
