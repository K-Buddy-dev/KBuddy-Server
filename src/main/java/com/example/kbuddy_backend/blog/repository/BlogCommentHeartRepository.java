package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogCommentHeart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogCommentHeartRepository extends JpaRepository<BlogCommentHeart, Long> {
    Optional<BlogCommentHeart> findByCommentIdAndUserId(Long commentId, Long userId);
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
} 