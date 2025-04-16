package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogReportRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.response.AllBlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.service.BlogCommentService;
import com.example.kbuddy_backend.blog.service.BlogService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.kbuddy_backend.blog.constant.SortBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/blog")
@Tag(name = "Blog API", description = "블로그 API 목록")
public class BlogController {

    private final BlogService blogService;
    private final BlogCommentService blogCommentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "블로그 게시글 작성", description = "새로운 블로그 게시글을 작성합니다. blogSaveRequest 는 JSON 형식의 문자열로, 이미지는 선택적으로 multipart form data 로 전송합니다. <br> 임시 저장 글은 status를 DRAFT로 설정합니다.")
    public ResponseEntity<DefaultResponse> saveBlog(
            @Valid @RequestPart(value = "blogSaveRequest") BlogSaveRequest blogSaveRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.saveBlog(blogSaveRequest, images, user);
        return ResponseEntity.noContent().build(); // 204 No content 반환
    }

    @GetMapping
    @Operation(summary = "블로그 게시글 전체 조회", description = "블로그 게시글 전체 조회합니다. 카테고리 코드로 필터링 할 수 있습니다.")
    public ResponseEntity<AllBlogResponse> getAllBlog(@RequestParam(value = "size") int pageSize,
                                                      @RequestParam(value = "id", required = false) Long blogId,
                                                      @RequestParam(value = "keyword", defaultValue = "") String title,
                                                      @RequestParam(required = false, value = "sort") SortBy sortBy,
                                                      @RequestParam(required = false, value = "categoryCode") Integer categoryCode,
                                                      @Parameter(hidden = true) @CurrentUser User user) {
        AllBlogResponse allBlogResponse = blogService.getAllBlog(pageSize, blogId, title, sortBy, categoryCode, user);
        return ResponseEntity.ok().body(allBlogResponse);
    }

    // 특정 블로그를 조회합니다.
    @GetMapping("/{blogId}")
    @Operation(summary = "특정 블로그 게시글 조회", description = "블로그 게시글 id를 통해 조회합니다. 임시저장 글은 작성자만 조회 가능합니다.")
    public ResponseEntity<BlogResponse> getBlog(
            @PathVariable Long blogId,
            @Parameter(hidden = true) @CurrentUser User user) {
        BlogResponse blog = blogService.getBlog(blogId, user);
        return ResponseEntity.ok().body(blog);
    }

    // 블로그 내용을 수정합니다.
    @PatchMapping(value = "/{blogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "블로그 게시글 업데이트", description = "블로그 게시글을 업데이트 합니다. status, 이미지 추가/삭제 등을 포함합니다. <br> 임시 저장에서 게시글로 변경 시 status를 PUBLISHED로 설정합니다.")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable final Long blogId,
            @Valid @RequestPart(value = "blogUpdateRequest") BlogUpdateRequest blogUpdateRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> newFiles,
            @Parameter(hidden = true) @CurrentUser User user) {
        BlogResponse blog = blogService.updateBlog(blogId, blogUpdateRequest, newFiles, user);
        return ResponseEntity.ok(blog); // 200 OK, 수정된 블로그 데이터 반환
        // ok().body(blog) -> ok(blog)로 변경: ok() 자체가 argument가 통과했을때 body값과 함께 응답하기 때문에 굳이 .body()를 사용할 필요가 없다.
    }

    // 블로그를 삭제합니다.    
    @DeleteMapping("/{blogId}")
    @Operation(summary = "Blog 게시글 삭제", description = "Blog 게시글을 삭제합니다.")
    public ResponseEntity<Void> deleteBlog(@PathVariable final Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.deleteBlog(blogId, user);
        return ResponseEntity.noContent().build();
    }

    // 단일 블로그 게시글을 북마크에 추가합니다.
    @PostMapping("/{blogId}/bookmark")
    @Operation(summary = "Blog 게시글 즐겨찾기", description = "Blog 게시글을 사용자 즐겨찾기 목록에 추가합니다.")
    public ResponseEntity<Void> addBookmark(@PathVariable final Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.addBookmark(user, blogId);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    // 단일 블로그 게시글을 북마크에서 제거합니다.
    @DeleteMapping("/{blogId}/unbookmark")
    @Operation(summary = "Blog 게시글 즐겨찾기 삭제", description = "Blog 게시글을 사용자 즐겨찾기 목록에 추가된 항목을 삭제 합니다.")
    public ResponseEntity<Void> removeBookmark(@PathVariable final Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.removeBookmark(user, blogId);
        return ResponseEntity.noContent().build();
    }

    // 블로그에 댓글을 추가합니다.
    @PostMapping("/{blogId}/comments") // REST 원칙에 맞게 URI 수정
    @Operation(summary = "댓글 작성", description = "블로그 게시글에 댓글을 작성합니다.")
    public ResponseEntity<Void> saveComment(@PathVariable Long blogId,
                                            @Valid @RequestBody BlogCommentSaveRequest blogCommentSaveRequest,
                                                       @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.saveBlogComment(blogId, blogCommentSaveRequest, user);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    @PatchMapping("/{blogId}/comments/{commentId}") // REST 원칙에 맞게 URI 수정
    @Operation(summary = "댓글 수정", description = "블로그 게시글의 댓글을 수정합니다.")
    public ResponseEntity<DefaultResponse> updateBlogComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Valid @RequestBody BlogCommentSaveRequest blogCommentSaveRequest,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.updateBlogComment(blogId, commentId, blogCommentSaveRequest, user);
        return ResponseEntity.noContent().build();
    }

    // 블로그에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/hearts")
    @Operation(summary = "Blog 게시글 좋아요", description = "Blog 게시글에 좋아요를 1개 올립니다.")
    public ResponseEntity<Void> plusBlogHeart(@PathVariable Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.plusHeart(blogId, user);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    // 블로그의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/hearts")
    @Operation(summary = "Blog 게시글 좋아요 취소", description = "Blog 게시글의 좋아요를 1개 내립니다.")
    public ResponseEntity<Void> minusBlogHeart(@PathVariable Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.minusHeart(blogId, user);
        return ResponseEntity.noContent().build();
    }

    // 댓글에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요", description = "블로그 댓글에 좋아요를 1개 추가합니다.")
    public ResponseEntity<Void> plusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.plusHeart(commentId, user);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    // 댓글의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요 취소", description = "블로그 댓글의 좋아요를 1개 내립니다.")
    public ResponseEntity<Void> minusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.minusHeart(commentId, user);
        return ResponseEntity.noContent().build();
    }

    // 블로그를 신고합니다.
    @PostMapping("/{blogId}/report")
    @Operation(summary = "블로그 신고", description = "블로그 게시글을 신고합니다.")
    public ResponseEntity<String> reportBlog(
            @PathVariable Long blogId,
            @RequestBody BlogReportRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.reportBlog(blogId, request, user);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }
} 