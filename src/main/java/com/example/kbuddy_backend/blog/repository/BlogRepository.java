package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
