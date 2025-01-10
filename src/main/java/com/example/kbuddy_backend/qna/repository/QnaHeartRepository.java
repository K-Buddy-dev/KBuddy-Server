package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaHeart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaHeartRepository extends JpaRepository<QnaHeart,Long> {
    void deleteByQnaIdAndUserId(Long qnaId, Long userId);
    void deleteByQnaCommentIdAndUserId(Long qnaCommentId, Long userId);

    Optional<QnaHeart> findByQnaIdAndUserId(Long qnaId, Long userId);
    Optional<QnaHeart> findByQnaCommentIdAndUserId(Long qnaCommentId, Long userId);

}
