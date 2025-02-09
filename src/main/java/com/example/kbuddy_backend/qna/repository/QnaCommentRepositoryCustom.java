package com.example.kbuddy_backend.qna.repository;


import com.example.kbuddy_backend.qna.entity.QnaComment;
import java.util.List;

public interface QnaCommentRepositoryCustom {

    List<QnaComment> findAllCommentsByQnaId(Long qnaId);
}