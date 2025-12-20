package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QnaReportRepository extends JpaRepository<QnaReport, Long> {
    boolean existsByQnaIdAndReporterId(Long qnaId, Long reportId);
    List<QnaReport> findAllByQnaId(Long qnaId);

    @Query("""
            select qr.qna.id as qnaId,
                   qr.qna.title as qnaTitle,
                   qr.qna.writer.id as writerId,
                   qr.qna.writer.username as writerUsername,
                   qr.qna.writer.email as writerEmail,
                   count(qr.id) as reportCount
            from QnaReport qr
            group by qr.qna.id, qr.qna.title, qr.qna.writer.id, qr.qna.writer.username, qr.qna.writer.email
            order by count(qr.id) desc
            """)
    List<QnaReportSummary> findReportSummaries();
}
