package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaCategoryRepository extends JpaRepository<QnaCategory, Long> {


}