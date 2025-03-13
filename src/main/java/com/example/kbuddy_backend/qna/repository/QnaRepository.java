package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.Qna;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Long>,QnaRepositoryCustom{

}