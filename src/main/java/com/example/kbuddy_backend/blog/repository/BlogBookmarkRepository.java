package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BlogBookmarkRepository extends JpaRepository<BlogBookmark, Long> {

    @Query("""
            select bookmark.blog.id
            from BlogBookmark bookmark
            where bookmark.user.id = :userId
              and bookmark.blog.id in :blogIds
            """)
    Set<Long> findBookmarkedBlogIds(@Param("userId") Long userId, @Param("blogIds") List<Long> blogIds);

    Optional<BlogBookmark> findByBlog(Blog blog);

    Optional<BlogBookmark> findByBlogIdAndUserId(Long BlogId, Long Userid);

    boolean existsByBlogIdAndUserId(Long BlogId, Long Userid);
    
    List<BlogBookmark> findByUserId(Long userId);
} 