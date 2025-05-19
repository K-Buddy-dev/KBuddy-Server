package com.example.kbuddy_backend.qna.controller;

import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.dto.response.QnaCommentResponse;
import com.example.kbuddy_backend.qna.service.QnaCommentService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/qna")
@Tag(name = "QnA Comment API", description = "QnA 댓글 API 목록")
public class QnaCommentController {

    private final QnaCommentService qnaCommentService;

    @PostMapping("/{qnaId}/comment")
    @Operation(summary = "QnA 댓글 작성", description = "QnA에 댓글을 작성합니다.")
    public ResponseEntity<Void> saveQnaComment(
            @PathVariable Long qnaId,
            @RequestBody QnaCommentSaveRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        qnaCommentService.saveQnaComment(qnaId, request, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/comment/{commentId}")
    @Operation(summary = "QnA 댓글 수정", description = "QnA 댓글을 수정합니다.")
    public ResponseEntity<Void> updateQnaComment(
            @PathVariable Long commentId,
            @RequestBody QnaCommentSaveRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        qnaCommentService.updateQnaComment(commentId, request, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "QnA 댓글 삭제", description = "QnA 댓글을 삭제합니다.")
    public ResponseEntity<Void> deleteQnaComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        qnaCommentService.deleteQnaComment(commentId, user);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{qnaId}/comments")
    @Operation(summary = "QnA 댓글 목록 조회", description = "QnA의 댓글 목록을 조회합니다.")
    public ResponseEntity<List<QnaCommentResponse>> findQnaComments(@PathVariable Long qnaId) {
        List<QnaCommentResponse> comments = qnaCommentService.findQnaComments(qnaId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comment/{parentId}/replies")
    @Operation(summary = "QnA 대댓글 목록 조회", description = "QnA 댓글의 대댓글 목록을 조회합니다.")
    public ResponseEntity<List<QnaCommentResponse>> findQnaCommentReplies(@PathVariable Long parentId) {
        List<QnaCommentResponse> replies = qnaCommentService.findQnaCommentReplies(parentId);
        return ResponseEntity.ok(replies);
    }
} 