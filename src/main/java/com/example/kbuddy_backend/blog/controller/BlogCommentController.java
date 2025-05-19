package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogCommentResponse;
import com.example.kbuddy_backend.blog.service.BlogCommentService;
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
@RequestMapping("/kbuddy/v1/blog")
@Tag(name = "Blog Comment API", description = "블로그 댓글 API 목록")
public class BlogCommentController {

    private final BlogCommentService blogCommentService;

    @PostMapping("/{blogId}/comment")
    @Operation(summary = "블로그 댓글 작성", description = "블로그에 댓글을 작성합니다.")
    public ResponseEntity<Void> saveBlogComment(
            @PathVariable Long blogId,
            @RequestBody BlogCommentSaveRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.saveBlogComment(blogId, request, user);
        return ResponseEntity.ok().build();
    }



    @PutMapping("/comment/{commentId}")
    @Operation(summary = "블로그 댓글 수정", description = "블로그 댓글을 수정합니다.")
    public ResponseEntity<Void> updateBlogComment(
            @PathVariable Long commentId,
            @RequestBody BlogCommentSaveRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.updateBlogComment(commentId, request, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "블로그 댓글 삭제", description = "블로그 댓글을 삭제합니다.")
    public ResponseEntity<Void> deleteBlogComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.deleteBlogComment(commentId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comment/{commentId}/reply")
    @Operation(summary = "블로그 대댓글 삭제", description = "블로그 대댓글을 삭제합니다.")
    public ResponseEntity<Void> deleteBlogCommentReply(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.deleteBlogCommentReply(commentId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{blogId}/comments")
    @Operation(summary = "블로그 댓글 목록 조회", description = "블로그의 댓글 목록을 조회합니다.")
    public ResponseEntity<List<BlogCommentResponse>> findBlogComments(@PathVariable Long blogId) {
        List<BlogCommentResponse> comments = blogCommentService.findBlogComments(blogId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comment/{parentId}/replies")
    @Operation(summary = "블로그 대댓글 목록 조회", description = "블로그 댓글의 대댓글 목록을 조회합니다.")
    public ResponseEntity<List<BlogCommentResponse>> findBlogCommentReplies(@PathVariable Long parentId) {
        List<BlogCommentResponse> replies = blogCommentService.findBlogCommentReplies(parentId);
        return ResponseEntity.ok(replies);
    }
} 