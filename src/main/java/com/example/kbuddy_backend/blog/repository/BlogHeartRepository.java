package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogHeart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BlogHeartRepository extends JpaRepository<BlogHeart, Long> {
    @Query("""
            select heart.blog.id
            from BlogHeart heart
            where heart.user.id = :userId
              and heart.blog.id in :blogIds
            """)
    Set<Long> findHeartedBlogIds(@Param("userId") Long userId, @Param("blogIds") List<Long> blogIds);

    void deleteByBlogIdAndUserId(Long blogId, Long userId);

    void deleteByBlogCommentIdAndUserId(Long blogCommentId, Long userId);

    Optional<BlogHeart> findByBlogCommentIdAndUserId(Long blogCommentId, Long userId);
    Optional<BlogHeart> findByBlogIdAndUserId(Long blogId, Long userId);

    boolean existsByBlogIdAndUserId(Long blogId, Long userId);
} 