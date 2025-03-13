package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogReplyRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogCommentResponse;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.entity.BlogCommentHeart;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import com.example.kbuddy_backend.blog.exception.BlogCommentNotFoundException;
import com.example.kbuddy_backend.blog.exception.BlogNotFoundException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogHeartException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogBookmarkException;
import com.example.kbuddy_backend.blog.exception.BlogBookmarkNotFoundException;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.blog.repository.BlogCommentHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogBookmarkRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kbuddy_backend.blog.constant.SortBy;
import com.example.kbuddy_backend.blog.dto.response.BlogListResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final BlogHeartRepository blogHeartRepository;
    private final BlogCommentHeartRepository blogCommentHeartRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;

    // 새로운 블로그를 저장
    @Transactional
    public void saveBlog(BlogSaveRequest request, User user) {
        Blog blog = Blog.builder()
                .writer(user)
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .imageUrls(request.imageUrls())
                .build();
        blogRepository.save(blog);
    }

    // 특정 블로그를 조회하고 조회수를 증가
    public BlogResponse getBlog(Long blogId) {
        Blog blog = findBlogById(blogId);
        blog.plusViewCount();
        return makeBlogResponse(blog);
    }

    /**
     * 블로그 목록을 페이징하여 조회
     * @param pageable 페이지 정보
     * @return 페이징된 블로그 목록
     */
    public Page<BlogResponse> getBlogs(Pageable pageable) {
        return blogRepository.findAllByOrderByIdDesc(pageable)
                .map(this::makeBlogResponse);
    }

    /**
     * 블로그 내용을 수정합니다.
     * @param blogId 수정할 블로그 ID
     * @param request 수정할 내용
     * @param user 수정 요청자
     */
    @Transactional
    public void updateBlog(Long blogId, BlogUpdateRequest request, User user) {
        Blog blog = findBlogById(blogId);
        validateWriter(blog, user);
        blog.update(request.title(), request.content(), request.category(), request.imageUrls());
    }

    /**
     * 블로그를 삭제합니다.
     * @param blogId 삭제할 블로그 ID
     * @param user 삭제 요청자
     */
    @Transactional
    public void deleteBlog(Long blogId, User user) {
        Blog blog = findBlogById(blogId);
        validateWriter(blog, user);
        blogRepository.delete(blog);
    }

    /**
     * 블로그에 댓글을 추가합니다.
     * @param blogId 댓글을 추가할 블로그 ID
     * @param request 댓글 내용
     * @param user 댓글 작성자
     */
    @Transactional
    public void saveComment(Long blogId, BlogCommentRequest request, User user) {
        Blog blog = findBlogById(blogId);
        BlogComment comment = BlogComment.builder()
                .blog(blog)
                .writer(user)
                .content(request.content())
                .build();
        blog.addComment(comment);
        blogCommentRepository.save(comment);
    }

    // 블로그의 댓글을 삭제합니다.
    @Transactional
    public void deleteComment(Long blogId, Long commentId, User user) {
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new BlogNotFoundException());
        validateWriter(comment, user);
        
        comment.delete();
        
        // 대댓글이 없는 경우에만 실제로 삭제
        if (comment.getReplies().isEmpty()) {
            blogCommentRepository.delete(comment);
        }
    }

    // 블로그에 좋아요를 추가합니다.
    @Transactional
    public void plusHeart(Long blogId, User user) {
        blogHeartRepository.findByBlogIdAndUserId(blogId, user.getId())
                .ifPresent(heart -> {
                    throw new DuplicatedBlogHeartException(); // 좋아요를 이미 누른 경우
                });
        Blog blog = findBlogById(blogId);
        BlogHeart heart = new BlogHeart(user, blog);
        blog.plusHeart(heart);
        blogHeartRepository.save(heart);
    }

    //블로그의 좋아요를 취소
    @Transactional
    public void minusHeart(Long blogId, User user) {
        Blog blog = findBlogById(blogId);
        BlogHeart heart = blogHeartRepository.findByBlogIdAndUserId(blogId, user.getId())
                .orElseThrow(BlogNotFoundException::new);
        blog.minusHeart(heart);
        blogHeartRepository.deleteByBlogIdAndUserId(blogId, user.getId());
    }

    /**
     * 블로그를 신고
     * @param blogId 신고할 블로그 ID
     */
    @Transactional
    public void reportBlog(Long blogId) {
        Blog blog = findBlogById(blogId);
        blog.plusReportCount();
    }

    /**
     * 댓글에 대댓글을 추가합니다.
     */
    @Transactional
    public void saveReply(Long blogId, Long commentId, BlogReplyRequest request, User user) {
        Blog blog = findBlogById(blogId);
        BlogComment parentComment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new BlogCommentNotFoundException());
        
        if (parentComment.isReply()) {
            throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
        }

        BlogComment reply = BlogComment.builder()
                .blog(blog)
                .writer(user)
                .parent(parentComment)
                .content(request.content())
                .build();

        blogCommentRepository.save(reply);
    }

    /**
     * 댓글에 좋아요를 추가합니다.
     */
    @Transactional
    public void plusCommentHeart(Long blogId, Long commentId, User user) {
        blogCommentHeartRepository.findByCommentIdAndUserId(commentId, user.getId())
                .ifPresent(heart -> {
                    throw new DuplicatedBlogHeartException();
                });

        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new BlogCommentNotFoundException());

        BlogCommentHeart heart = new BlogCommentHeart(user, comment);
        comment.plusHeart(heart);
        blogCommentHeartRepository.save(heart);
    }

    /**
     * 댓글의 좋아요를 취소합니다.
     */
    @Transactional
    public void minusCommentHeart(Long blogId, Long commentId, User user) {
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new BlogCommentNotFoundException());

        BlogCommentHeart heart = blogCommentHeartRepository.findByCommentIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new BlogNotFoundException());

        comment.minusHeart(heart);
        blogCommentHeartRepository.deleteByCommentIdAndUserId(commentId, user.getId());
    }

    /**
     * 블로그를 북마크에 추가합니다.
     */
    @Transactional
    public void addBookmark(Long blogId, User user) {
        blogBookmarkRepository.findByBlogIdAndUserId(blogId, user.getId())
                .ifPresent(bookmark -> {
                    throw new DuplicatedBlogBookmarkException();
                });

        Blog blog = findBlogById(blogId);
        BlogBookmark bookmark = new BlogBookmark(user, blog);
        blogBookmarkRepository.save(bookmark);
    }

    /**
     * 블로그를 북마크에서 제거합니다.
     */
    @Transactional
    public void removeBookmark(Long blogId, User user) {
        blogBookmarkRepository.findByBlogIdAndUserId(blogId, user.getId())
                .orElseThrow(() -> new BlogBookmarkNotFoundException());
        blogBookmarkRepository.deleteByBlogIdAndUserId(blogId, user.getId());
    }

    /**
     * 사용자의 북마크 목록을 조회합니다.
     */
    public Page<BlogResponse> getBookmarks(User user, Pageable pageable) {
        return blogBookmarkRepository.findAllByUserId(user.getId(), pageable)
                .map(bookmark -> makeBlogResponse(bookmark.getBlog()));
    }

    /**
     * 블로그 ID로 블로그를 조회
     * @param blogId 조회할 블로그 ID
     * @return 블로그 엔티티
     * @throws BlogNotFoundException 블로그를 찾을 수 없는 경우
     */
    private Blog findBlogById(Long blogId) {
        return blogRepository.findById(blogId)
                .orElseThrow(BlogNotFoundException::new);
    }

    /**
     * 블로그 엔티티를 응답 DTO로 변환합니다.
     * @param blog 블로그 엔티티
     * @return 블로그 응답 DTO
     */
    private BlogResponse makeBlogResponse(Blog blog) {
        List<BlogCommentResponse> commentResponses = blog.getComments().stream()
                .filter(comment -> comment.getParent() == null)
                .map(this::makeCommentResponse)
                .toList();

        return BlogResponse.of(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getWriter().getUsername(),
                blog.getCategory(),
                blog.getImageUrls(),
                blog.getHeartCount(),
                blog.getComments().size(),
                blog.getViewCount(),
                commentResponses,
                blog.getCreatedDate()
        );
    }

    /**
     * 댓글 엔티티를 응답 DTO로 변환합니다.
     * @param comment 댓글 엔티티
     * @return 댓글 응답 DTO
     */
    private BlogCommentResponse makeCommentResponse(BlogComment comment) {
        return BlogCommentResponse.of(
                comment.getId(),
                comment.getContent(),
                comment.getWriter().getUsername(),
                comment.getHeartCount(),
                comment.isReply(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getReplies().stream()
                        .map(this::makeCommentResponse)
                        .toList(),
                comment.getCreatedDate(),
                comment.isDeleted()
        );
    }

    // 블로그 작성자 검증
    private void validateWriter(Blog blog, User user) {
        if (!blog.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    // 댓글 작성자 검증
    private void validateWriter(BlogComment comment, User user) {
        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
    }

    /**
     * No-offset 방식으로 블로그 목록을 조회합니다.
     * @param pageSize 페이지 크기
     * @param lastBlogId 마지막으로 조회한 블로그 ID
     * @param keyword 검색 키워드
     * @param sortBy 정렬 조건
     * @return 블로그 목록 응답
     */
    public BlogListResponse getBlogsNoOffset(int pageSize, Long lastBlogId, String keyword, SortBy sortBy) {
        // pageSize + 1을 조회하여 다음 페이지 존재 여부 확인
        List<Blog> blogs = blogRepository.paginationNoOffset(lastBlogId, keyword, pageSize + 1, sortBy);
        
        boolean hasNext = blogs.size() > pageSize;
        if (hasNext) {
            blogs.remove(pageSize); // 다음 페이지 확인용으로 가져온 마지막 항목 제거
        }
        
        List<BlogResponse> blogResponses = blogs.stream()
                .map(this::makeBlogResponse)
                .toList();
                
        Long nextCursor = hasNext && !blogs.isEmpty() ? blogs.get(blogs.size() - 1).getId() : null;
        
        return BlogListResponse.of(blogResponses, hasNext, nextCursor);
    }

    private Long getNextId(List<BlogResponse> responses) {
        return responses.stream()
                .map(BlogResponse::id)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    @Transactional
    public void updateComment(Long blogId, Long commentId, BlogCommentRequest request, User user) {
        Blog blog = findBlogById(blogId);
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new BlogCommentNotFoundException());
                
        validateWriter(comment, user);
        comment.updateContent(request.content());
    }
} 