package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.exception.BlogNotFoundException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogHeartException;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final BlogHeartRepository blogHeartRepository;

    // 새로운 블로그를 저장
    @Transactional
    public void saveBlog(BlogSaveRequest request, User user) {
        Blog blog = Blog.builder()
                .writer(user)
                .title(request.title())
                .content(request.content())
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
        blog.update(request.title(), request.content());
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
        blogCommentRepository.delete(comment);
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
     * 블로그 엔티티를 응답 DTO로 변환
     * @param blog 블로그 엔티티
     * @return 블로그 응답 DTO
     */
    private BlogResponse makeBlogResponse(Blog blog) {
        return BlogResponse.of(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getWriter().getUsername(),
                blog.getHeartCount(),
                blog.getComments().size(),
                blog.getViewCount(),
                blog.getCreatedDate()
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
} 