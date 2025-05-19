package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogCommentResponse;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.exception.BlogCommentNotFoundException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogCommentHeartException;
import com.example.kbuddy_backend.blog.exception.NotWriterException;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.user.entity.User;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogCommentService {

    private final BlogCommentRepository blogCommentRepository;
    private final BlogHeartRepository blogHeartRepository;

    private final BlogService blogService;

    @Transactional
    public void saveBlogComment(Long blogId, BlogCommentSaveRequest request, User user) {
        Blog blog = blogService.findBlogById(blogId);
        BlogComment blogComment = BlogComment.builder()
                .content(request.content())
                .blog(blog)
                .writer(user)
                .build();
        blogCommentRepository.save(blogComment);
    }

    @Transactional
    public void updateBlogComment(Long commentId, BlogCommentSaveRequest request, User user) {
        BlogComment comment = findBlogCommentById(commentId);
        if (!Objects.equals(comment.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
        comment.updateContent(request.content());
    }


    @Transactional
    public void deleteBlogComment( Long commentId, User user) {
        BlogComment comment = findBlogCommentById(commentId);
        if (!Objects.equals(comment.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
        blogCommentRepository.delete(comment);
    }

    @Transactional
    public void deleteBlogCommentReply(Long commentId, User user) {
        BlogComment reply = findBlogCommentById(commentId);
        if (!Objects.equals(reply.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
        if (!reply.isReply()) {
            throw new IllegalArgumentException("대댓글만 삭제할 수 있습니다.");
        }
        blogCommentRepository.delete(reply);
    }

    @Transactional
    public void plusHeart(Long commentId, User user) {
        blogHeartRepository.findByBlogCommentIdAndUserId(commentId, user.getId())
                .ifPresent(blogHeart -> {
                    throw new DuplicatedBlogCommentHeartException();
                });
        BlogComment blogComment = findBlogCommentById(commentId);
        BlogHeart blogHeart = new BlogHeart(user, blogComment);
        blogComment.plusHeart(blogHeart);
        blogHeartRepository.save(blogHeart);
    }

    @Transactional
    public void minusHeart(Long commentId, User user) {
        BlogComment blogComment = findBlogCommentById(commentId);
        BlogHeart byBlogIdAndUserId = blogHeartRepository.findByBlogCommentIdAndUserId(commentId, user.getId())
                .orElseThrow(BlogCommentNotFoundException::new);
        blogComment.minusHeart(byBlogIdAndUserId);
        blogHeartRepository.deleteByBlogCommentIdAndUserId(commentId, user.getId());
    }

    private BlogComment findBlogCommentById(Long commentId) {
        return blogCommentRepository.findById(commentId)
                .orElseThrow(BlogCommentNotFoundException::new);
    }

    public List<BlogCommentResponse> findBlogComments(Long blogId) {
        List<BlogComment> comments = blogCommentRepository.findByBlogIdAndParentIsNullOrderByCreatedDateDesc(blogId);
        return comments.stream()
                .map(BlogCommentResponse::of)
                .collect(Collectors.toList());
    }

    public List<BlogCommentResponse> findBlogCommentReplies(Long parentId) {
        List<BlogComment> replies = blogCommentRepository.findByParentIdOrderByCreatedDateDesc(parentId);
        return replies.stream()
                .map(BlogCommentResponse::of)
                .collect(Collectors.toList());
    }
}
