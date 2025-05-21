package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaComment;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface QnaCommentRepositoryCustom {
    List<QnaComment> findCommentsWithWriterAndChildren(Long qnaId);
    Map<Long, Long> findCommentHeartCounts(List<Long> commentIds);
    Set<Long> findHeartedCommentIds(List<Long> commentIds, Long userId);
}