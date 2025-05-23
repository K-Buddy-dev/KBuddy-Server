package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.dto.request.BlogCommentSaveRequest;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.exception.BlogCommentNotFoundException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogCommentHeartException;
import com.example.kbuddy_backend.common.exception.MaximumReplyDepthExceededException;
import com.example.kbuddy_backend.blog.exception.NotWriterException;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.user.entity.User;

import java.util.Objects;

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

		BlogComment parent = null;
		if (request.parentId() != null) {
			parent = blogCommentRepository.findById(request.parentId())
				.orElseThrow(BlogCommentNotFoundException::new);
			// 대댓글의 대댓글 방지
			if (parent.isReply()) {
				throw new MaximumReplyDepthExceededException();
			}
		}

		BlogComment blogComment = BlogComment.builder()
			.content(request.content())
			.blog(blog)
			.writer(user)
			.build();

		if (parent != null) {
			parent.addChild(blogComment);
		}
		
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
	public void deleteBlogComment(Long commentId, User user) {
		BlogComment comment = findBlogCommentById(commentId);
		if (!Objects.equals(comment.getWriter().getId(), user.getId())) {
			throw new NotWriterException();
		}
		blogCommentRepository.delete(comment);
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
}
