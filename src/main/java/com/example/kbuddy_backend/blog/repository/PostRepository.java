package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
