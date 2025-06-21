package com.example.kbuddy_backend.qna.controller;

import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.service.QnaCommentService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/qna")
@Tag(name = "QnA API", description = "QnA API 목록")
public class QnaCommentController {

	private final QnaCommentService qnaCommentService;

	@PostMapping("/{qnaId}/comment")
	@Operation(summary = "QnA 댓글 작성", description = "QnA에 댓글을 작성합니다. 대댓글 작성 : parentId 필요, 댓글 작성 : parentId를 null로 설정")
	public ResponseEntity<Void> saveQnaComment(
		@PathVariable Long qnaId,
		@RequestBody QnaCommentSaveRequest request,
		@Parameter(hidden = true) @CurrentUser User user) {
		qnaCommentService.saveQnaComment(qnaId, request, user);
		return ResponseEntity.noContent().build(); // 204 No content 반환
	}

	@PutMapping("/{qnaId}/comment/{commentId}")
	@Operation(summary = "QnA 댓글 수정", description = "QnA 댓글을 수정합니다.")
	public ResponseEntity<Void> updateQnaComment(
		@PathVariable Long qnaId,
		@PathVariable Long commentId,
		@RequestBody QnaCommentSaveRequest request,
		@Parameter(hidden = true) @CurrentUser User user) {
		qnaCommentService.updateQnaComment(commentId, request, user);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/comment/{commentId}")
	@Operation(summary = "QnA 댓글 삭제", description = "QnA 댓글을 삭제합니다.")
	public ResponseEntity<Void> deleteQnaComment(
		@PathVariable Long commentId,
		@Parameter(hidden = true) @CurrentUser User user) {
		qnaCommentService.deleteQnaComment(commentId, user);
		return ResponseEntity.noContent().build();
	}

	//댓글 좋아요
	@PostMapping("/comment/{commentId}/hearts")
	@Operation(summary = "댓글 좋아요", description = "Q&A 게시글 댓글에 좋아요를 1개 올립니다.")
	public ResponseEntity<String> plusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
		qnaCommentService.plusHeart(commentId, user);
		return ResponseEntity.noContent().build(); // 204 No content 반환
	}

	//댓글 좋아요 취소
	@DeleteMapping("/comment/{commentId}/hearts")
	@Operation(summary = "댓글 좋아요 취소", description = "Q&A 게시글 댓글에 좋아요를 1개 내립니다.")
	public ResponseEntity<Void> minusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
		qnaCommentService.minusHeart(commentId, user);
		return ResponseEntity.noContent().build();
	}
} 