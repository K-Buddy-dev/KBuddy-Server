package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogImageRepository extends JpaRepository<BlogImage, Long> {

    void deleteByFilePath(String filePath);
}
