package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Long>,QnaRepositoryCustom{

    // Find Q&As by status with pagination
    Page<Qna> findByStatus(QnaStatus status, Pageable pageable);

    // Find Q&As by writer and status with pagination
    Page<Qna> findByWriterAndStatus(User writer, QnaStatus status, Pageable pageable);

    // Find ALL Q&As by writer and status (no pagination)
    List<Qna> findByWriterAndStatus(User writer, QnaStatus status);
    
    // Find bookmarked Q&As by user ID
    @Query("SELECT q FROM Qna q JOIN QnaBookmark qb ON q.id = qb.qna.id WHERE qb.user.id = :userId AND q.status = 'PUBLISHED' ORDER BY q.createdDate DESC")
    List<Qna> findBookmarkedQnasByUserId(@Param("userId") Long userId);
}