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
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategoryId(request.getCategoryId());
        post.setUser(user);
        postRepository.save(post);

        return new PostCreateResponse(post);
    }
}
