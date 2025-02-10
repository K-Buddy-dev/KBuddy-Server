package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaCommentRepository extends JpaRepository<QnaComment, Long>{
    Optional<QnaComment> findByIdAndWriterId(Long commentId, Long userId);

    List<QnaComment> findAllByQnaIdOrderByCreatedDateAsc(Long qnaId);
}