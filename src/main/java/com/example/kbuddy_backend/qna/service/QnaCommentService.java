package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaComment;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.exception.DuplicatedQnaCommentHeartException;
import com.example.kbuddy_backend.qna.exception.NotWriterException;
import com.example.kbuddy_backend.qna.exception.QnaCommentNotFoundException;
import com.example.kbuddy_backend.qna.repository.QnaCommentRepository;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QnaCommentService {


    private final QnaCommentRepository qnaCommentRepository;
    private final QnaHeartRepository qnaHeartRepository;

    private final QnaService qnaService;

    @Transactional
    public void saveQnaComment(Long qnaId,QnaCommentSaveRequest qnaCommentSaveRequest, User user) {
        final Qna qna = qnaService.findQnaById(qnaId);
        QnaComment qnaComment = QnaComment.builder()
                .content(qnaCommentSaveRequest.content())
                .qna(qna)
                .writer(user)
                .build();
        qnaCommentRepository.save(qnaComment);
    }

    @Transactional
    public void plusHeart(Long commentId, User user) {
        qnaHeartRepository.findByQnaCommentIdAndUserId(commentId, user.getId())
                .ifPresent(qnaHeart -> {
                    throw new DuplicatedQnaCommentHeartException();
                });
        QnaComment qnaComment = findQnaCommentById(commentId);
        QnaHeart qnaHeart = new QnaHeart(user, qnaComment);
        qnaComment.plusHeart(qnaHeart);
        qnaHeartRepository.save(qnaHeart);
    }

    @Transactional
    public void minusHeart(Long commentId, User user) {
        QnaComment qnaComment = findQnaCommentById(commentId);
        QnaHeart byQnaIdAndUserId = qnaHeartRepository.findByQnaCommentIdAndUserId(commentId, user.getId()).orElseThrow(QnaCommentNotFoundException::new);
        qnaComment.minusHeart(byQnaIdAndUserId);
        qnaHeartRepository.deleteByQnaCommentIdAndUserId(commentId, user.getId());
    }

    private QnaComment findQnaCommentById(Long commentId) {
        return qnaCommentRepository.findById(commentId)
                .orElseThrow(QnaCommentNotFoundException::new);
    }

    @Transactional
    public void updateQnaComment(Long qnaId, Long commentId, QnaCommentSaveRequest qnaCommentSaveRequest, User user) {
        Qna qnaById = qnaService.findQnaById(qnaId);
        QnaComment qnaComment = findQnaCommentById(commentId);
        isQnaCommentWriter(user, qnaById);
        qnaComment.updateContent(qnaCommentSaveRequest.content());
    }

    private static void isQnaCommentWriter(User user, Qna qnaById) {
        if (!Objects.equals(qnaById.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
    }
}
