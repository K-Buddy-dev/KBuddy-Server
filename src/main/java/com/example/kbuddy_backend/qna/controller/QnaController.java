package com.example.kbuddy_backend.qna.controller;


import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.service.QnaCommentService;
import com.example.kbuddy_backend.qna.service.QnaService;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("kbuddy/v1/qna")
@Tag(name = "Q&A API", description = "Q&A API 목록")
public class QnaController {

    private final QnaService qnaService;
    private final QnaCommentService qnaCommentService;

    //todo: 응답 dto 추가

    @PostMapping
    @Operation(summary = "Q&A 게시글 작성", description = "Q&A 게시글을 작성 합니다.")
    public ResponseEntity<DefaultResponse> saveQna(@RequestBody QnaSaveRequest qnaSaveRequest, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.saveQna(qnaSaveRequest, user);
        return ResponseEntity.ok().body(DefaultResponse.of(true,"게시글 작성 성공"));

    }

    @GetMapping("/{qnaId}")
    @Operation(summary = "특정 Q&A 게시글 조회", description = "Q&A 게시글 id를 통해 조회 합니다.")
    public ResponseEntity<QnaResponse> getQna(@PathVariable Long qnaId) {
        QnaResponse qna = qnaService.getQna(qnaId);
        return ResponseEntity.ok().body(qna);
    }

    @PostMapping("/{qnaId}/comment")
    @Operation(summary = "댓글 작성", description = "Q&A 게시글에서 댓글을 작성 합니다.")
    public ResponseEntity<?> saveQnaComment(@PathVariable Long qnaId,@RequestBody QnaCommentSaveRequest qnaCommentSaveRequest,
                                            @Parameter(hidden = true) @CurrentUser User user) {
        qnaCommentService.saveQnaComment(qnaId,qnaCommentSaveRequest, user);
        return ResponseEntity.ok().body("success");
    }

    //Qna 좋아요
    @PostMapping("/{qnaId}/hearts")
    @Operation(summary = "Q&A 게시글 좋아요", description = "Q&A 게시글에 좋아요를 1개 올립니다.")
    public ResponseEntity<?> plusQnaHeart(@PathVariable Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.plusHeart(qnaId, user);
        return ResponseEntity.ok().body("success");
    }

    //Qna 좋아요 취소
    @Operation(summary = "Q&A 게시글 좋아요 취소", description = "Q&A 게시글에 좋아요를 1개 내립니다.")
    @DeleteMapping("/{qnaId}/hearts")
    public ResponseEntity<?> minusQnaHeart(@PathVariable Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.minusHeart(qnaId, user);
        return ResponseEntity.ok().body("success");
    }

    //댓글 좋아요
    @PostMapping("/comment/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요", description = "Q&A 게시글 댓글에 좋아요를 1개 올립니다.")
    public ResponseEntity<?> plusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaCommentService.plusHeart(commentId, user);
        return ResponseEntity.ok().body("success");
    }

    //댓글 좋아요 취소
    @DeleteMapping("/comment/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요 취소", description = "Q&A 게시글 댓글에 좋아요를 1개 내립니다.")
    public ResponseEntity<?> minusCommentHeart(@PathVariable Long commentId,@Parameter(hidden = true) @CurrentUser User user) {
        qnaCommentService.minusHeart(commentId, user);
        return ResponseEntity.ok().body("success");
    }
}
