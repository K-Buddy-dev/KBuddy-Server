package com.example.kbuddy_backend.qna.controller;


import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.dto.request.BookmarkRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaCommentSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaUpdateRequest;
import com.example.kbuddy_backend.qna.dto.response.AllQnaResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.service.QnaCommentService;
import com.example.kbuddy_backend.qna.service.QnaService;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.Builder.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * todo:DefaultResponse로 통일
 * 
 */
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

    //전체 조회 (페이징)
    @GetMapping
    @Operation(summary = "Q&A 게시글 전체 조회", description = "Q&A 게시글을 전체 조회 합니다.")
    public ResponseEntity<AllQnaResponse> getAllQna(@RequestParam(value = "size") int pageSize,
                                                    @RequestParam(value = "id", required = false) Long qnaId,
                                                    @RequestParam(value = "keyword") String title,
                                                    @RequestParam(required = false, value = "sort")
                                                    SortBy sortBy) {
        AllQnaResponse allQnaResponse = qnaService.getAllQna(pageSize, qnaId, title, sortBy);
        return ResponseEntity.ok().body(allQnaResponse);
    }

    @GetMapping("/{qnaId}")
    @Operation(summary = "특정 Q&A 게시글 조회", description = "Q&A 게시글 id를 통해 조회 합니다.")
    public ResponseEntity<QnaResponse> getQna(@PathVariable Long qnaId) {
        QnaResponse qna = qnaService.getQna(qnaId);
        return ResponseEntity.ok().body(qna);
    }

    @PatchMapping("/{qnaId}")
    @Operation(summary = "Q&A 게시글 업데이트", description = "Q&A 게시글을 업데이트 합니다.")
    public ResponseEntity<QnaResponse> updateQna(@PathVariable final Long qnaId,
                                                 @RequestBody QnaUpdateRequest qnaUpdateRequest,
                                                 @Parameter(hidden = true) @CurrentUser User user) {
        QnaResponse qna = qnaService.updateQna(qnaId, qnaUpdateRequest, user);
        return ResponseEntity.ok().body(qna);
    }

    @PostMapping("/{qnaId}/images")
    @Operation(summary = "Q&A 게시글 이미지 추가", description = "Q&A 게시글에 이미지를 추가 합니다.")
    public ResponseEntity<String> addQnaImages(@PathVariable final Long qnaId, @RequestPart List<ImageFileDto> images,
                                               @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.addImages(qnaId, images, user);
        return ResponseEntity.ok().body("이미지가 성공적으로 추가되었습니다.");
    }

    @DeleteMapping("/{qnaId}/images")
    @Operation(summary = "Q&A 게시글 이미지 삭제", description = "Q&A 게시글에 포함된 이미지를 삭제 합니다.")
    public ResponseEntity<String> deleteQnaImages(@PathVariable final Long qnaId,
                                                  @RequestBody List<ImageFileDto> images,
                                                  @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.deleteImages(qnaId, images, user);
        return ResponseEntity.ok().body("이미지가 성공적으로 삭제되었습니다.");
    }

    @DeleteMapping("/{qnaId}")
    @Operation(summary = "Q&A 게시글 삭제", description = "Q&A 게시글에 포함된 이미지를 삭제 합니다.")
    public ResponseEntity<String> deleteQna(@PathVariable final Long qnaId, @Parameter(hidden = true) @CurrentUser User user) {
        qnaService.deleteQna(qnaId, user);
        return ResponseEntity.ok().body("QnA가 성공적으로 삭제되었습니다.");
    }

    //단일 QnA 컨텐츠 북마크
    @PostMapping("/{qnaId}/bookmark")
    @Operation(summary = "Q&A 게시글 즐겨찾기", description = "Q&A 게시글을 사용자 즐겨찾기 목록에 추가 합니다.")
    public ResponseEntity<String> addBookmark(@RequestBody BookmarkRequest bookmarkRequest,
                                              @PathVariable final Long qnaId) {
        qnaService.addBookmark(bookmarkRequest, qnaId);
        return ResponseEntity.ok().body("성공적으로 북마크 하였습니다.");
    }

    //단일 QnA 컨텐츠 북마크 해제
    @PostMapping("/{qnaId}/unbookmark")
    @Operation(summary = "Q&A 게시글 즐겨찾기 삭제", description = "Q&A 게시글을 사용자 즐겨찾기 목록에 추가된 항목을 삭제 합니다.")
    public ResponseEntity<String> removeBookmark(@RequestBody BookmarkRequest bookmarkRequest,
                                                 @PathVariable final Long qnaId) {
        qnaService.removeBookmark(bookmarkRequest, qnaId);
        return ResponseEntity.ok().body("성공적으로 북마크 해제 하였습니다.");
    }


    @PostMapping("/{qnaId}/comments")
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
