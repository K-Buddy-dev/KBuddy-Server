package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogReplyRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogListResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.service.BlogService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.kbuddy_backend.blog.constant.SortBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/blog")
@Tag(name = "Blog API", description = "블로그 API 목록")
public class BlogController {

    private final BlogService blogService;

    /**
     * 새로운 블로그를 생성합니다.
     * @param request 블로그 생성 요청 정보 (제목, 내용)
     * @param user 현재 인증된 사용자
     * @return 블로그 생성 결과
     */
    @PostMapping
    @Operation(summary = "블로그 작성", description = "새로운 블로그 게시글을 작성합니다.")
    public ResponseEntity<DefaultResponse> saveBlog(
            @RequestBody BlogSaveRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.saveBlog(request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 작성 성공"));
    }

    // 특정 블로그를 조회합니다.
    @GetMapping("/{blogId}")
    @Operation(summary = "블로그 조회", description = "특정 블로그 게시글을 조회합니다.")
    public ResponseEntity<BlogResponse> getBlog(@PathVariable Long blogId) {
        return ResponseEntity.ok(blogService.getBlog(blogId));
    }

    // 블로그 내용을 수정합니다.
    @PatchMapping("/{blogId}")
    @Operation(summary = "블로그 수정", description = "블로그 게시글을 수정합니다.")
    public ResponseEntity<DefaultResponse> updateBlog(
            @PathVariable Long blogId,
            @RequestBody BlogUpdateRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.updateBlog(blogId, request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 수정 성공"));
    }

    // 블로그를 삭제합니다.    
    @DeleteMapping("/{blogId}")
    @Operation(summary = "블로그 삭제", description = "블로그 게시글을 삭제합니다.")
    public ResponseEntity<DefaultResponse> deleteBlog(
            @PathVariable Long blogId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.deleteBlog(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 삭제 성공"));
    }

    // 블로그에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/hearts")
    @Operation(summary = "블로그 좋아요", description = "블로그 게시글에 좋아요를 추가합니다.")
    public ResponseEntity<DefaultResponse> plusHeart(
            @PathVariable Long blogId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.plusHeart(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "좋아요 성공"));
    }

    // 블로그의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/hearts")
    @Operation(summary = "블로그 좋아요 취소", description = "블로그 게시글의 좋아요를 취소합니다.")
    public ResponseEntity<DefaultResponse> minusHeart(
            @PathVariable Long blogId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.minusHeart(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "좋아요 취소 성공"));
    }

    // 블로그에 댓글을 추가합니다.
    @PostMapping("/{blogId}/comment")
    @Operation(summary = "댓글 작성", description = "블로그 게시글에 댓글을 작성합니다.")
    public ResponseEntity<DefaultResponse> saveComment(
            @PathVariable Long blogId,
            @RequestBody BlogCommentRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.saveComment(blogId, request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 작성 성공"));
    }

    // 블로그의 댓글을 삭제합니다.
    @DeleteMapping("/{blogId}/comment/{commentId}")
    @Operation(summary = "댓글 삭제", description = "블로그 게시글의 댓글을 삭제합니다.")
    public ResponseEntity<DefaultResponse> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.deleteComment(blogId, commentId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 삭제 성공"));
    }

    // 블로그를 신고합니다.
    @PostMapping("/{blogId}/report")
    @Operation(summary = "블로그 신고", description = "블로그 게시글을 신고합니다.")
    public ResponseEntity<DefaultResponse> reportBlog(@PathVariable Long blogId) {
        blogService.reportBlog(blogId);
        return ResponseEntity.ok(DefaultResponse.of(true, "신고 성공"));
    }


    // 댓글에 대댓글을 추가합니다.
    @PostMapping("/{blogId}/comment/{commentId}/reply")
    @Operation(summary = "답글 작성", description = "블로그 댓글에 답글을 작성합니다.")
    public ResponseEntity<DefaultResponse> saveReply(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @RequestBody BlogReplyRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.saveReply(blogId, commentId, request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "답글 작성 성공"));
    }

    // 댓글에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/comment/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요", description = "블로그 댓글에 좋아요를 추가합니다.")
    public ResponseEntity<DefaultResponse> plusCommentHeart(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.plusCommentHeart(blogId, commentId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 좋아요 성공"));
    }

    // 댓글의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/comment/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요 취소", description = "블로그 댓글의 좋아요를 취소합니다.")
    public ResponseEntity<DefaultResponse> minusCommentHeart(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.minusCommentHeart(blogId, commentId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 좋아요 취소 성공"));
    }

    // 블로그를 북마크에 추가합니다.
    @PostMapping("/{blogId}/bookmarks")
    @Operation(summary = "북마크 추가", description = "블로그 게시글을 북마크에 추가합니다.")
    public ResponseEntity<DefaultResponse> addBookmark(
            @PathVariable Long blogId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.addBookmark(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "북마크 추가 성공"));
    }

    // 블로그를 북마크에서 제거합니다.
    @DeleteMapping("/{blogId}/bookmarks")
    @Operation(summary = "북마크 제거", description = "블로그 게시글을 북마크에서 제거합니다.")
    public ResponseEntity<DefaultResponse> removeBookmark(
            @PathVariable Long blogId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.removeBookmark(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "북마크 제거 성공"));
    }

    // 사용자의 북마크 목록을 조회합니다.
    @GetMapping("/bookmarks")
    @Operation(summary = "북마크 목록 조회", description = "사용자의 북마크 목록을 조회합니다.")
    public ResponseEntity<Page<BlogResponse>> getBookmarks(
            @Parameter(hidden = true) @CurrentUser User user,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.getBookmarks(user, pageable));
    }

    /**
     * No-offset 방식으로 블로그 목록을 조회합니다.
     * @param pageSize 페이지 크기
     * @param lastBlogId 마지막으로 조회한 블로그 ID (첫 페이지는 null)
     * @param keyword 검색 키워드 (제목, 내용)
     * @param sortBy 정렬 조건 (VIEW_COUNT, HEART_COUNT, COMMENT_COUNT, LATEST)
     */
    @GetMapping
    @Operation(summary = "블로그 목록 조회", description = "블로그 게시글 목록을 조회합니다.")
    public ResponseEntity<BlogListResponse> getAllBlogs(
            @RequestParam(value = "size") @Parameter(description = "페이지 크기") int pageSize,
            @RequestParam(value = "id", required = false) @Parameter(description = "마지막으로 조회한 블로그 ID") Long lastBlogId,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "검색 키워드") String keyword,
            @RequestParam(required = false, value = "sort") @Parameter(description = "정렬 기준 (VIEW_COUNT, HEART_COUNT, COMMENT_COUNT, LATEST)") SortBy sortBy) {
        BlogListResponse response = blogService.getBlogsNoOffset(pageSize, lastBlogId, keyword, sortBy);
        return ResponseEntity.ok().body(response);
    }
} 