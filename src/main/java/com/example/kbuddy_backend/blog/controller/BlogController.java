package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/blog")
public class BlogController {

    private final BlogService blogService;

    /**
     * 블로그 목록을 페이징하여 조회합니다.
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징된 블로그 목록
     */
    @GetMapping("/list")
    public ResponseEntity<Page<BlogResponse>> getBlogs(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.getBlogs(pageable));
    }

    /**
     * 새로운 블로그를 생성합니다.
     * @param request 블로그 생성 요청 정보 (제목, 내용)
     * @param user 현재 인증된 사용자
     * @return 블로그 생성 결과
     */
    @PostMapping
    public ResponseEntity<DefaultResponse> saveBlog(
            @RequestBody BlogSaveRequest request,
            @CurrentUser User user) {
        blogService.saveBlog(request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 작성 성공"));
    }

    /**
     * 특정 블로그를 조회합니다.
     * @param blogId 조회할 블로그 ID
     * @return 블로그 상세 정보
     */
    @GetMapping("/{blogId}")
    public ResponseEntity<BlogResponse> getBlog(@PathVariable Long blogId) {
        return ResponseEntity.ok(blogService.getBlog(blogId));
    }

    /**
     * 블로그 내용을 수정합니다.
     * @param blogId 수정할 블로그 ID
     * @param request 수정할 내용 (제목, 내용)
     * @param user 현재 인증된 사용자
     * @return 수정 결과
     */
    @PatchMapping("/{blogId}")
    public ResponseEntity<DefaultResponse> updateBlog(
            @PathVariable Long blogId,
            @RequestBody BlogUpdateRequest request,
            @CurrentUser User user) {
        blogService.updateBlog(blogId, request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 수정 성공"));
    }

    // 블로그를 삭제합니다.    
    @DeleteMapping("/{blogId}")
    public ResponseEntity<DefaultResponse> deleteBlog(
            @PathVariable Long blogId,
            @CurrentUser User user) {
        blogService.deleteBlog(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 삭제 성공"));
    }

    // 블로그에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/hearts")
    public ResponseEntity<DefaultResponse> plusHeart(
            @PathVariable Long blogId,
            @CurrentUser User user) {
        blogService.plusHeart(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "좋아요 성공"));
    }

    // 블로그의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/hearts")
    public ResponseEntity<DefaultResponse> minusHeart(
            @PathVariable Long blogId,
            @CurrentUser User user) {
        blogService.minusHeart(blogId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "좋아요 취소 성공"));
    }

    // 블로그에 댓글을 추가합니다.
    @PostMapping("/{blogId}/comment")
    public ResponseEntity<DefaultResponse> saveComment(
            @PathVariable Long blogId,
            @RequestBody BlogCommentRequest request,
            @CurrentUser User user) {
        blogService.saveComment(blogId, request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 작성 성공"));
    }

    // 블로그의 댓글을 삭제합니다.
    @DeleteMapping("/{blogId}/comment/{commentId}")
    public ResponseEntity<DefaultResponse> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @CurrentUser User user) {
        blogService.deleteComment(blogId, commentId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 삭제 성공"));
    }

    // 블로그를 신고합니다.
    @PostMapping("/{blogId}/report")
    public ResponseEntity<DefaultResponse> reportBlog(@PathVariable Long blogId) {
        blogService.reportBlog(blogId);
        return ResponseEntity.ok(DefaultResponse.of(true, "신고 성공"));
    }
} 