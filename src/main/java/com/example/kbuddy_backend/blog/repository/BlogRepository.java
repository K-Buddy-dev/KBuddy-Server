package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long>, BlogRepositoryCustom {
    Page<Blog> findAllByOrderByIdDesc(Pageable pageable);
} 