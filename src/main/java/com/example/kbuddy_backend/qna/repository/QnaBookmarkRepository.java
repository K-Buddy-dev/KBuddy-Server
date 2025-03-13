package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.Qna;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kbuddy_backend.qna.entity.QnaBookmark;

public interface QnaBookmarkRepository extends JpaRepository<QnaBookmark, Long> {

    Optional<QnaBookmark> findByQna(Qna qna);


}