package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaReportRepository extends JpaRepository<QnaReport, Long> {
    boolean existsByQnaIdAndReporterId(Long qnaId, Long reportId);
    List<QnaReport> findAllByQnaId(Long qnaId);
}
