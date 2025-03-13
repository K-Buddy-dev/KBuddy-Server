package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaCollectionRepository extends JpaRepository<QnaCollection, Long> {

}