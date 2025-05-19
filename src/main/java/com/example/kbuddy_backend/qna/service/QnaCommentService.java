package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.dto.response.QnaCommentResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaComment;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.exception.DuplicatedQnaCommentHeartException;
import com.example.kbuddy_backend.qna.exception.NotWriterException;
import com.example.kbuddy_backend.qna.exception.QnaCommentNotFoundException;
import com.example.kbuddy_backend.qna.exception.QnaCommentReplyNotAllowedException;
import com.example.kbuddy_backend.qna.repository.QnaCommentRepository;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QnaCommentService {

    private final QnaCommentRepository qnaCommentRepository;
    private final QnaHeartRepository qnaHeartRepository;
    private final QnaService qnaService;

    @Transactional
    public void saveQnaComment(Long qnaId, QnaCommentSaveRequest request, User user) {
        Qna qna = qnaService.findQnaById(qnaId);
        QnaComment comment = createQnaComment(qna, request, user);
        qnaCommentRepository.save(comment);
    }

    private QnaComment createQnaComment(Qna qna, QnaCommentSaveRequest request, User user) {
        if (request.parentId() != null) {
            return createQnaCommentReply(qna, request, user);
        }
        return QnaComment.builder()
                .content(request.content())
                .qna(qna)
                .writer(user)
                .build();
    }

    private QnaComment createQnaCommentReply(Qna qna, QnaCommentSaveRequest request, User user) {
        QnaComment parentComment = findQnaCommentById(request.parentId());
        if (parentComment.isReply()) {
            throw new QnaCommentReplyNotAllowedException("대댓글에는 답글을 달 수 없습니다.");
        }

        return QnaComment.builder()
                .content(request.content())
                .qna(qna)
                .writer(user)
                .parent(parentComment)
                .build();
    }

    @Transactional
    public void updateQnaComment(Long commentId, QnaCommentSaveRequest request, User user) {
        QnaComment comment = findQnaCommentById(commentId);
        if (!Objects.equals(comment.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
        comment.updateContent(request.content());
    }

    @Transactional
    public void deleteQnaComment(Long commentId, User user) {
        QnaComment comment = findQnaCommentById(commentId);
        if (!Objects.equals(comment.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
        qnaCommentRepository.delete(comment);
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
        QnaHeart byQnaIdAndUserId = qnaHeartRepository.findByQnaCommentIdAndUserId(commentId, user.getId())
                .orElseThrow(QnaCommentNotFoundException::new);
        qnaComment.minusHeart(byQnaIdAndUserId);
        qnaHeartRepository.deleteByQnaCommentIdAndUserId(commentId, user.getId());
    }

    private QnaComment findQnaCommentById(Long commentId) {
        return qnaCommentRepository.findById(commentId)
                .orElseThrow(QnaCommentNotFoundException::new);
    }

    public List<QnaCommentResponse> findQnaComments(Long qnaId) {
        List<QnaComment> comments = qnaCommentRepository.findCommentsWithWriterAndChildren(qnaId);
        return comments.stream()
                .map(comment -> {
                    List<QnaCommentResponse> replies = comment.getChildren().stream()
                            .map(reply -> QnaCommentResponse.of(
                                    reply.getId(),
                                    reply.getQna().getId(),
                                    reply.getWriter().getId(),
                                    reply.getContent(),
                                    reply.getCreatedDate(),
                                    reply.getLastModifiedDate(),
                                    List.of()
                            ))
                            .toList();

                    return QnaCommentResponse.of(
                            comment.getId(),
                            comment.getQna().getId(),
                            comment.getWriter().getId(),
                            comment.getContent(),
                            comment.getCreatedDate(),
                            comment.getLastModifiedDate(),
                            replies
                    );
                })
                .collect(Collectors.toList());
    }

    public List<QnaCommentResponse> findQnaCommentReplies(Long parentId) {
        List<QnaComment> replies = qnaCommentRepository.findByParentIdOrderByCreatedDateDesc(parentId);
        return replies.stream()
                .map(reply -> QnaCommentResponse.of(
                        reply.getId(),
                        reply.getQna().getId(),
                        reply.getWriter().getId(),
                        reply.getContent(),
                        reply.getCreatedDate(),
                        reply.getLastModifiedDate(),
                        List.of()
                ))
                .collect(Collectors.toList());
    }
}
