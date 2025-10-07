package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.common.exception.MaximumReplyDepthExceededException;
import com.example.kbuddy_backend.notification.service.FCMService;
import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaComment;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.exception.DuplicatedQnaCommentHeartException;
import com.example.kbuddy_backend.qna.exception.NotWriterException;
import com.example.kbuddy_backend.qna.exception.QnaCommentNotFoundException;
import com.example.kbuddy_backend.qna.repository.QnaCommentRepository;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.service.UserBlockService;
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
	private final UserBlockService userBlockService;
    private final FCMService fcmService;

	@Transactional
	public void saveQnaComment(Long qnaId, QnaCommentSaveRequest request, User user) {
		Qna qna = qnaService.findQnaById(qnaId);
		
		// 차단된 사용자의 게시글에 댓글을 작성하려는 경우
		if (userBlockService.isBlocked(user, qna.getWriter())) {
			throw new IllegalArgumentException("차단된 사용자의 게시글에는 댓글을 작성할 수 없습니다.");
		}

		QnaComment parent = null;
		if (request.parentId() != null) {
			parent = qnaCommentRepository.findById(request.parentId())
				.orElseThrow(QnaCommentNotFoundException::new);
			
			// 차단된 사용자의 댓글에 답글을 작성하려는 경우
			if (userBlockService.isBlocked(user, parent.getWriter())) {
				throw new IllegalArgumentException("차단된 사용자의 댓글에는 답글을 작성할 수 없습니다.");
			}
			
			if (parent.isReply()) {
				throw new MaximumReplyDepthExceededException();
			}
		}

		QnaComment qnaComment = QnaComment.builder()
			.content(request.content())
			.qna(qna)
			.writer(user)
			.build();

		if (parent != null) {
			parent.addChild(qnaComment);
            //대댓글 알림
            if (!Objects.equals(parent.getWriter().getId(), user.getId())) {
                String title = "New Reply to Your Comment";
                String body = user.getUsername() + "has replied to your comment.";
                fcmService.sendNotificationAllFcmTokens(parent.getWriter(), title, body, "qna", qna.getId().toString());
            }
		}
		
		qnaCommentRepository.save(qnaComment);

        //알림 전송
        if (!Objects.equals(qna.getWriter().getId(), user.getId())) {
            String title = "New Comment on Your Q&A Post";
            String body = user.getUsername() + " has left a comment on your post.";
            fcmService.sendNotificationAllFcmTokens(qna.getWriter(), title, body, "qna", qna.getId().toString());
        }
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

        //알림 전송
        if (!Objects.equals(qnaComment.getWriter().getId(), user.getId())) {
            String title = "Your Q&A Comment Got a New Like";
            String body = user.getUsername() + " liked your comment.";
            fcmService.sendNotificationAllFcmTokens(qnaComment.getWriter(), title, body, "qna", qnaComment.getQna().getId().toString());
        }
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
}
