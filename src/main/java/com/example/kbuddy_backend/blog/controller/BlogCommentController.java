package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.service.BlogCommentService;
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
@RequestMapping("/kbuddy/v1/blog")
@Tag(name = "Blog API", description = "블로그 API 목록")
public class BlogCommentController {

	private final BlogCommentService blogCommentService;

	@PostMapping("/{blogId}/comment")
	@Operation(summary = "Blog 댓글 작성", description = "블로그 게시글에 댓글을 작성합니다. 대댓글 작성 : parentId 필요, 댓글 작성 : parentId를 null로 설정")
	public ResponseEntity<Void> saveBlogComment(
		@PathVariable Long blogId,
		@RequestBody BlogCommentSaveRequest request,
		@Parameter(hidden = true) @CurrentUser User user) {
		blogCommentService.saveBlogComment(blogId, request, user);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/comment/{commentId}")
	@Operation(summary = "Blog 댓글 수정", description = "블로그 게시글의 댓글을 수정합니다.")
	public ResponseEntity<Void> updateBlogComment(
		@PathVariable Long commentId,
		@RequestBody BlogCommentSaveRequest request,
		@Parameter(hidden = true) @CurrentUser User user) {
		blogCommentService.updateBlogComment(commentId, request, user);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/comment/{commentId}")
	@Operation(summary = "Blog 댓글 삭제", description = "블로그 게시글의 댓글을 삭제합니다.")
	public ResponseEntity<Void> deleteBlogComment(
		@PathVariable Long commentId,
		@Parameter(hidden = true) @CurrentUser User user) {
		blogCommentService.deleteBlogComment(commentId, user);
		return ResponseEntity.noContent().build();
	}

	// 댓글에 좋아요를 추가합니다.
	@PostMapping("/{blogId}/{commentId}/hearts")
	@Operation(summary = "댓글 좋아요", description = "블로그 댓글에 좋아요를 1개 추가합니다.")
	public ResponseEntity<Void> plusCommentHeart(@PathVariable Long commentId,
		@Parameter(hidden = true) @CurrentUser User user) {
		blogCommentService.plusHeart(commentId, user);
		return ResponseEntity.noContent().build(); // 204 No Content 반환
	}

	// 댓글의 좋아요를 취소합니다.
	@DeleteMapping("/{blogId}/{commentId}/hearts")
	@Operation(summary = "댓글 좋아요 취소", description = "블로그 댓글의 좋아요를 1개 내립니다.")
	public ResponseEntity<Void> minusCommentHeart(@PathVariable Long commentId,
		@Parameter(hidden = true) @CurrentUser User user) {
		blogCommentService.minusHeart(commentId, user);
		return ResponseEntity.noContent().build();
	}

} 