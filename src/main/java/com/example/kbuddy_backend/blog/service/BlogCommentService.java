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
import com.example.kbuddy_backend.notification.service.FCMService;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.service.UserBlockService;

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
	private final UserBlockService userBlockService;
    private final FCMService fcmService;

	@Transactional
	public void saveBlogComment(Long blogId, BlogCommentSaveRequest request, User user) {
		Blog blog = blogService.findBlogById(blogId);

		// 차단된 사용자의 게시글에 댓글을 작성하려는 경우
		if (userBlockService.isBlocked(user, blog.getWriter())) {
			throw new IllegalArgumentException("차단된 사용자의 게시글에는 댓글을 작성할 수 없습니다.");
		}

		BlogComment parent = null;
		if (request.parentId() != null) {
			parent = blogCommentRepository.findById(request.parentId())
				.orElseThrow(BlogCommentNotFoundException::new);
			
			// 차단된 사용자의 댓글에 답글을 작성하려는 경우
			if (userBlockService.isBlocked(user, parent.getWriter())) {
				throw new IllegalArgumentException("차단된 사용자의 댓글에는 답글을 작성할 수 없습니다.");
			}
			
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
            //대댓글 알림
            if (!Objects.equals(parent.getWriter().getId(), user.getId())) {
                String title = "New Reply to Your Comment";
                String body = user.getUsername() + "has replied to your comment.";
                fcmService.sendNotificationAllFcmTokens(parent.getWriter(), title, body);
            }
		}
		
		blogCommentRepository.save(blogComment);

        //알림 전송
        if (!Objects.equals(blog.getWriter().getId(), user.getId())) {
            String title = "New Comment on Your Blog Post";
            String body = user.getUsername() + "has left a comment on your post.";
            fcmService.sendNotificationAllFcmTokens(blog.getWriter(), title, body);
        }

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

        //알림 전송
        if (!Objects.equals(blogComment.getWriter().getId(), user.getId())) {
            String title = "Your  Comment Got a New Like";
            String body = user.getUsername() + "liked your comment.";
            fcmService.sendNotificationAllFcmTokens(blogComment.getWriter(), title, body);
        }
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
