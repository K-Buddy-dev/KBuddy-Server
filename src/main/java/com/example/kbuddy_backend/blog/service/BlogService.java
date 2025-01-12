package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCreateReq;
import com.example.kbuddy_backend.blog.dto.response.BlogCreateRes;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    @Transactional
    public BlogCreateRes createBlog(BlogCreateReq request, User user) {

        Blog savedBlog = blogRepository.save(new Blog(request.title(), request.content(),user));
        return BlogCreateRes.of(savedBlog.getId(), savedBlog.getWriter().getId());
    }
}
