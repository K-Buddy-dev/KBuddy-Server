package com.example.kbuddy_backend.qna.controller;


import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaReportRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaUpdateRequest;
import com.example.kbuddy_backend.qna.dto.response.AllQnaResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.service.QnaCommentService;
import com.example.kbuddy_backend.qna.service.QnaService;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * todo:DefaultResponse로 통일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("kbuddy/v1/qna")
@Tag(name = "QnA API", description = "QnA API 목록")
public class QnaController {

    private final QnaService qnaService;
    private final QnaCommentService qnaCommentService;

    //todo: 응답 dto 추가
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Q&A 게시글 작성", description = "Q&A 게시글을 작성합니다. qnaSaveRequest는 JSON 형식의 문자열로, 이미지는 선택적으로 multipart form data로 전송합니다. <br> 임시 저장 글은 status를 DRAFT로 설정합니다.")
    public ResponseEntity<DefaultResponse> saveQna(
            @Valid @RequestPart(value = "qnaSaveRequest") QnaSaveRequest qnaSaveRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.saveQna(qnaSaveRequest, images, user);
        return ResponseEntity.noContent().build(); // 204 No content 반환
    }

    //전체 조회 (페이징)
    @GetMapping
    @Operation(summary = "Q&A 게시글 전체 조회", description = "Q&A 게시글을 전체 조회 합니다. 카테고리 코드로 필터링할 수 있습니다.")
    public ResponseEntity<AllQnaResponse> getAllQna(@RequestParam(value = "size") int pageSize,
                                                    @RequestParam(value = "id", required = false) Long qnaId,
                                                    @RequestParam(value = "keyword", defaultValue = "") String title,
                                                    @RequestParam(required = false, value = "sort") SortBy sortBy,
                                                    @RequestParam(required = false, value = "categoryCode") Integer categoryCode,
                                                    @Parameter(hidden = true) @CurrentUser(required = false) User user) {
        AllQnaResponse allQnaResponse = qnaService.getAllQna(pageSize, qnaId, title, sortBy, categoryCode, user);
        return ResponseEntity.ok().body(allQnaResponse);
    }

    @GetMapping("/{qnaId}")
    @Operation(summary = "특정 Q&A 게시글 조회", description = "Q&A 게시글 id를 통해 조회 합니다. 임시저장 글은 작성자만 조회 가능합니다.")
    public ResponseEntity<QnaResponse> getQna(
            @PathVariable Long qnaId,
            @Parameter(hidden = true) @CurrentUser(required = false) User user) {
        QnaResponse qna = qnaService.getQna(qnaId, user);
        return ResponseEntity.ok().body(qna);
    }

    @PatchMapping(value = "/{qnaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Q&A 게시글 업데이트", description = "Q&A 게시글을 업데이트 합니다. status, 이미지 추가/삭제 등을 포함합니다. <br> 임시 저장에서 게시글로 변경 시 status를 PUBLISHED로 설정합니다.")
    public ResponseEntity<QnaResponse> updateQna(
            @PathVariable final Long qnaId,
            @Valid @RequestPart(value = "qnaUpdateRequest") QnaUpdateRequest qnaUpdateRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> newFiles,
            @Parameter(hidden = true) @CurrentUser User user) {
        QnaResponse qna = qnaService.updateQna(qnaId, qnaUpdateRequest, newFiles, user);
        return ResponseEntity.ok().body(qna);
    }

    @DeleteMapping("/{qnaId}")
    @Operation(summary = "Q&A 게시글 삭제", description = "Q&A 게시글을 삭제 합니다.")
    public ResponseEntity<Void> deleteQna(@PathVariable final Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.deleteQna(qnaId, user);
        return ResponseEntity.noContent().build();
    }

    //단일 QnA 컨텐츠 북마크
    @PostMapping("/{qnaId}/bookmark")
    @Operation(summary = "Q&A 게시글 즐겨찾기", description = "Q&A 게시글을 사용자 즐겨찾기 목록에 추가 합니다.")
    public ResponseEntity<Void> addBookmark(@PathVariable final Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.addBookmark(user, qnaId);
        return ResponseEntity.noContent().build();
    }

    //단일 QnA 컨텐츠 북마크 해제
    @DeleteMapping("/{qnaId}/unbookmark")
    @Operation(summary = "Q&A 게시글 즐겨찾기 삭제", description = "Q&A 게시글을 사용자 즐겨찾기 목록에 추가된 항목을 삭제 합니다.")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable final Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.removeBookmark(user, qnaId);
        return ResponseEntity.noContent().build();
    }

    //Qna 좋아요
    @PostMapping("/{qnaId}/hearts")
    @Operation(summary = "Q&A 게시글 좋아요", description = "Q&A 게시글에 좋아요를 1개 올립니다.")
    public ResponseEntity<Void> plusQnaHeart(@PathVariable Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.plusHeart(qnaId, user);
        return ResponseEntity.noContent().build(); // 204 No content 반환
    }

    //Qna 좋아요 취소
    @Operation(summary = "Q&A 게시글 좋아요 취소", description = "Q&A 게시글에 좋아요를 1개 내립니다.")
    @DeleteMapping("/{qnaId}/hearts")
    public ResponseEntity<Void> minusQnaHeart(@PathVariable Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.minusHeart(qnaId, user);
        return ResponseEntity.noContent().build(); // 204 No content 반환
    }

    //Qna 신고
    @PostMapping("/{qnaId}/report")
    @Operation(summary = "Q&A 게시글 신고", description = "Qna 게시글을 신고합니다.")
    public ResponseEntity<String> reportQna(@PathVariable Long qnaId, @RequestBody QnaReportRequest request, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.reportQna(qnaId, request, user);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }
}