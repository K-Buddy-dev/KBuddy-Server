package com.example.kbuddy_backend.blog.controller;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogReportRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogBookmarkRequest;
import com.example.kbuddy_backend.blog.dto.response.AllBlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.service.BlogCommentService;
import com.example.kbuddy_backend.blog.service.BlogService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    private final BlogCommentService blogCommentService;

    @PostMapping
    @Operation(summary = "블로그 게시글 작성", description = "새로운 블로그 게시글을 작성합니다.")
    public ResponseEntity<DefaultResponse> saveBlog(@RequestBody BlogSaveRequest blogSaveRequest, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.saveBlog(blogSaveRequest, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "블로그 작성 성공"));
    }

    @GetMapping
    @Operation(summary = "블로그 게시글 전체 조회", description = "블로그 게시글 전체 조회합니다.")
    public ResponseEntity<AllBlogResponse> getAllBlog(@RequestParam(value = "size") int pageSize,
                                                      @RequestParam(value = "id", required = false) Long blogId,
                                                      @RequestParam(value = "keyword", required = false) String title,
                                                      @RequestParam(required = false, value = "sort")
                                                          SortBy sortBy) {
        AllBlogResponse allBlogResponse = blogService.getAllBlog(pageSize, blogId, title, sortBy);
        return ResponseEntity.ok().body(allBlogResponse);
    }

    // 특정 블로그를 조회합니다.
    @GetMapping("/{blogId}")
    @Operation(summary = "특정 블로그 게시글 조회", description = "블로그 게시글 id를 통해 조회합니다.")
    public ResponseEntity<BlogResponse> getBlog(@PathVariable Long blogId) {
        BlogResponse blog = blogService.getBlog(blogId);
        return ResponseEntity.ok().body(blog);
    }

    // 블로그 내용을 수정합니다.
    @PatchMapping("/{blogId}")
    @Operation(summary = "블로그 게시글 업데이트", description = "블로그 게시글을 업데이트 합니다.")
    public ResponseEntity<BlogResponse> updateBlog(@PathVariable Long blogId,
                                                      @RequestBody BlogUpdateRequest blogUpdateRequest,
                                                      @Parameter(hidden = true) @CurrentUser User user) {
        BlogResponse blog = blogService.updateBlog(blogId, blogUpdateRequest, user);
        return ResponseEntity.ok().body(blog);
    }

    @PostMapping("/{blogId}/images")
    @Operation(summary = "Blog 게시글 이미지 추가", description = "Blog 게시글에 이미지를 추가 합니다.")
    public ResponseEntity<String> addBlogImages(@PathVariable final Long blogId, @RequestPart List<ImageFileDto> images,
                                               @Parameter(hidden = true) @CurrentUser User user) {
        blogService.addImages(blogId, images, user);
        return ResponseEntity.ok().body("이미지가 성공적으로 추가되었습니다.");
    }

    @DeleteMapping("/{blogId}/images")
    @Operation(summary = "Blog 게시글 이미지 삭제", description = "Blog 게시글에 포함된 이미지를 삭제 합니다.")
    public ResponseEntity<String> deleteBlogImages(@PathVariable final Long blogId,
                                                   @RequestBody List<ImageFileDto> images,
                                                   @Parameter(hidden = true) @CurrentUser User user) {
        blogService.deleteImages(blogId, images, user);
        return ResponseEntity.ok().body("이미지가 성공적으로 삭제되었습니다.");
    }

    // 블로그를 삭제합니다.    
    @DeleteMapping("/{blogId}")
    @Operation(summary = "Blog 게시글 삭제", description = "Blog 게시글을 삭제합니다.")
    public ResponseEntity<String> deleteBlog(@PathVariable final Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.deleteBlog(blogId, user);
        return ResponseEntity.ok().body("Blog 게시글이 성공적으로 삭제되었습니다");
    }

    // 단일 블로그 게시글을 북마크에 추가합니다.
    @PostMapping("/{blogId}/bookmark")
    @Operation(summary = "Blog 게시글 즐겨찾기", description = "Blog 게시글을 사용자 즐겨찾기 목록에 추가합니다.")
    public ResponseEntity<String> addBookmark(@RequestBody BlogBookmarkRequest blogBookmarkRequest,
                                              @PathVariable final Long blogId) {
        blogService.addBookmark(blogBookmarkRequest, blogId);
        return ResponseEntity.ok().body("성공적으로 북마크 하였습니다.");
    }

    // 단일 블로그 게시글을 북마크에서 제거합니다.
    @PostMapping("{blogId}/unbookmark")
    @Operation(summary = "Blog 게시글 즐겨찾기 삭제", description = "Blog 게시글을 사용자 즐겨찾기 목록에 추가된 항목을 삭제 합니다.")
    public ResponseEntity<String> removeBookmark(@RequestBody BlogBookmarkRequest blogBookmarkRequest,
                                                 @PathVariable final Long blogId) {
        blogService.removeBookmark(blogBookmarkRequest, blogId);
        return ResponseEntity.ok().body("성공적으로 북마크 해제 하였습니다.");
    }

    // 블로그에 댓글을 추가합니다.
    @PostMapping("/{blogId}/comment")
    @Operation(summary = "댓글 작성", description = "블로그 게시글에 댓글을 작성합니다.")
    public ResponseEntity<?> saveComment(@PathVariable Long blogId, @RequestBody BlogCommentSaveRequest blogCommentSaveRequest,
                                                       @Parameter(hidden = true) @CurrentUser User user) {
        blogCommentService.saveBlogComment(blogId, blogCommentSaveRequest, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 작성 성공"));
    }

    // 블로그에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/hearts")
    @Operation(summary = "Blog 게시글 좋아요", description = "Blog 게시글에 좋아요를 1개 올립니다.")
    public ResponseEntity<?> plusBlogHeart(@PathVariable Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.plusHeart(blogId, user);
        return ResponseEntity.ok().body("블로그 좋아요 성공");
    }

    // 블로그의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/hearts")
    @Operation(summary = "Blog 게시글 좋아요 취소", description = "Blog 게시글의 좋아요를 1개 내립니다.")
    public ResponseEntity<?> minusBlogHeart(@PathVariable Long blogId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.minusHeart(blogId, user);
        return ResponseEntity.ok().body("블로그 좋아요 취소");
    }

    // 댓글에 좋아요를 추가합니다.
    @PostMapping("/{blogId}/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요", description = "블로그 댓글에 좋아요를 1개 추가합니다.")
    public ResponseEntity<?> plusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.plusHeart(commentId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 좋아요 성공"));
    }

    // 댓글의 좋아요를 취소합니다.
    @DeleteMapping("/{blogId}/{commentId}/hearts")
    @Operation(summary = "댓글 좋아요 취소", description = "블로그 댓글의 좋아요를 1개 내립니다.")
    public ResponseEntity<?> minusCommentHeart(@PathVariable Long commentId, @Parameter(hidden = true) @CurrentUser User user) {
        blogService.minusHeart(commentId, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 좋아요 취소 성공"));
    }

//    // 블로그의 댓글을 삭제합니다.
//    @DeleteMapping("/{blogId}/comment/{commentId}")
//    @Operation(summary = "댓글 삭제", description = "블로그 게시글의 댓글을 삭제합니다.")
//    public ResponseEntity<?> deleteComment(@PathVariable Long blogId, @RequestBody BlogCommentSaveRequest blogCommentSaveRequest,
//                                           @Parameter(hidden = true) @CurrentUser User user) {
//        blogCommentService.deleteBlogComment(blogId, blogCommentSaveRequest, user);
//        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 삭제 성공"));
//    }

    // 블로그를 신고합니다.
    @PostMapping("/{blogId}/report")
    @Operation(summary = "블로그 신고", description = "블로그 게시글을 신고합니다.")
    public ResponseEntity<DefaultResponse> reportBlog(
            @PathVariable Long blogId,
            @RequestBody BlogReportRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        blogService.reportBlog(blogId, request, user);
        return ResponseEntity.ok(DefaultResponse.of(true, "신고 접수 성공"));
    }

//    @PatchMapping("/{blogId}/comment/{commentId}")
//    @Operation(summary = "댓글 수정", description = "블로그 게시글의 댓글을 수정합니다.")
//    public ResponseEntity<DefaultResponse> updateComment(
//            @PathVariable Long blogId,
//            @PathVariable Long commentId,
//            @RequestBody BlogCommentSaveRequest request,
//            @Parameter(hidden = true) @CurrentUser User user) {
//        blogService.updateComment(blogId, commentId, request, user);
//        return ResponseEntity.ok(DefaultResponse.of(true, "댓글 수정 성공"));
//    }
} 