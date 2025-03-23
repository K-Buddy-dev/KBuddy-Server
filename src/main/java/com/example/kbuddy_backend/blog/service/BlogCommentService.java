package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.exception.BlogCommentNotFoundException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogCommentHeartException;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.user.entity.User;
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
    public void saveBlogComment(Long qnaId, BlogCommentSaveRequest blogCommentSaveRequest, User user) {
        final Blog blog = blogService.findBlogById(qnaId);
        BlogComment blogComment = BlogComment.builder()
                .content(blogCommentSaveRequest.content())
                .blog(blog)
                .writer(user)
                .build();
        blogCommentRepository.save(blogComment);
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
        BlogHeart byBlogIdAndUserId = blogHeartRepository.findByBlogCommentIdAndUserId(commentId, user.getId()).orElseThrow(BlogCommentNotFoundException::new);
        blogComment.minusHeart(byBlogIdAndUserId);
        blogHeartRepository.deleteByBlogCommentIdAndUserId(commentId, user.getId());
    }

    private BlogComment findBlogCommentById(Long commentId) {
        return blogCommentRepository.findById(commentId)
            .orElseThrow(BlogCommentNotFoundException::new);
    }
}
