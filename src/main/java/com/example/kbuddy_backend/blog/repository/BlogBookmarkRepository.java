package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogBookmarkRepository extends JpaRepository<BlogBookmark, Long> {
    Optional<BlogBookmark> findByBlog(Blog blog);
} 