package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCreateRequest;
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
  public Long createBlog(BlogCreateRequest request, User writer) {
    Blog blog = Blog.builder()
      .title(request.title())
      .content(request.content())
      .writer(writer)
      .build();

    Blog savedBlog = blogRepository.save(blog);
    return savedBlog.getId();
  }
}