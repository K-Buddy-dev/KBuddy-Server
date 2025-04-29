package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long>, BlogRepositoryCustom {

    // 작성자의 모든 블로그와 그 상태(임시저장 여부)를 조회합니다.(no pagination)
    List<Blog> findByWriterAndStatus(User user, BlogStatus blogStatus);
}