package com.example.kbuddy_backend.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kbuddy_backend.qna.entity.QnaImage;

public interface QnaImageRepository extends JpaRepository<QnaImage, Long> {

    void deleteByFilePath(String filePath);
}