package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCreateRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogCreateResponse;
import com.example.kbuddy_backend.blog.entity.BlogPost;
import com.example.kbuddy_backend.blog.repository.BlogPostRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogPostService {

    private final BlogPostRepository blogRepository;

    @Transactional
    public BlogCreateResponse createBlog(BlogCreateRequest request, User user) {

        BlogPost savedBlog = blogRepository.save(new BlogPost(request.title(), request.content(),user));
        return BlogCreateResponse.of(savedBlog.getId(), savedBlog.getWriter().getId());
    }
}
