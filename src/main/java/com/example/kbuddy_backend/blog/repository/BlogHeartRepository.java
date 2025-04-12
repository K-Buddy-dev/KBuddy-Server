package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogHeart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogHeartRepository extends JpaRepository<BlogHeart, Long> {
    void deleteByBlogIdAndUserId(Long blogId, Long userId);

    void deleteByBlogCommentIdAndUserId(Long blogCommentId, Long userId);

    Optional<BlogHeart> findByBlogCommentIdAndUserId(Long blogCommentId, Long userId);
    Optional<BlogHeart> findByBlogIdAndUserId(Long blogId, Long userId);

    boolean existsByBlogIdAndUserId(Long blogId, Long userId);
} 