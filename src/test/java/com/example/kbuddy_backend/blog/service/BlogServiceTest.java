package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogHeartException;
import com.example.kbuddy_backend.blog.exception.BlogNotFoundException;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogBookmarkRepository;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class BlogServiceTest extends IntegrationTest {

    @Autowired
    private BlogService blogService;

    @MockBean
    private BlogRepository blogRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BlogHeartRepository blogHeartRepository;

    @MockBean
    private BlogBookmarkRepository blogBookmarkRepository;

    @MockBean
    private BlogCommentRepository blogCommentRepository;

    private User user;
    private Long blogId;
    private Blog blog;

    @BeforeEach
    void setUp() {
        user = UserFixtures.createUser();
        blogId = 1L;
        blog = Blog.builder().build();
    }

    @DisplayName("게시된 글은 비로그인 사용자도 조회할 수 있고, 북마크/좋아요는 false로 내려간다.")
    @Test
    public void testGetBlog_PublishedIsVisibleToGuest() {
        // given
        Blog published = Blog.builder()
                .writer(user)
                .status(BlogStatus.PUBLISHED)
                .build();

        given(blogRepository.findById(blogId)).willReturn(Optional.of(published));

        // when
        BlogResponse response = blogService.getBlog(blogId, null);

        // then
        assertNotNull(response);
        assertFalse(response.isBookmarked());
        assertFalse(response.isHearted());
        verify(blogBookmarkRepository, never()).existsByBlogIdAndUserId(any(), any());
        verify(blogHeartRepository, never()).existsByBlogIdAndUserId(any(), any());
    }

    @DisplayName("비로그인 조회에서도 조회수는 증가한다.")
    @Test
    public void testGetBlog_ViewCountIncreasesForGuest() {
        // given
        Blog published = Blog.builder()
                .writer(user)
                .status(BlogStatus.PUBLISHED)
                .build();
        int before = published.getViewCount();

        given(blogRepository.findById(blogId)).willReturn(Optional.of(published));

        // when
        blogService.getBlog(blogId, null);

        // then
        assertEquals(before + 1, published.getViewCount());
    }

    @DisplayName("임시저장 글 접근이 차단되면 조회수는 증가하지 않는다.")
    @Test
    public void testGetBlog_ViewCountNotIncreasedWhenAccessDenied() {
        // given
        Blog draft = Blog.builder()
                .writer(user)
                .status(BlogStatus.DRAFT)
                .build();
        int before = draft.getViewCount();

        given(blogRepository.findById(blogId)).willReturn(Optional.of(draft));

        // when
        assertThrows(AccessDeniedException.class, () -> blogService.getBlog(blogId, null));

        // then
        assertEquals(before, draft.getViewCount());
    }

    @DisplayName("임시저장 글은 비로그인 사용자가 조회할 수 없다.")
    @Test
    public void testGetBlog_DraftIsNotVisibleToGuest() {
        // given
        Blog draft = Blog.builder()
                .writer(user)
                .status(BlogStatus.DRAFT)
                .build();

        given(blogRepository.findById(blogId)).willReturn(Optional.of(draft));

        // when & then
        assertThrows(AccessDeniedException.class, () -> blogService.getBlog(blogId, null));
    }

    @DisplayName("블로그 좋아요 테스트 - 이미 좋아요를 눌렀을 때 예외 발생")
    @Test
    public void testPlusHeart_AlreadyLiked() {
        // given
        BlogHeart existingBlogHeart = new BlogHeart(user, blog);

        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
                .willReturn(Optional.of(existingBlogHeart));

        // when & then
        assertThrows(DuplicatedBlogHeartException.class, () -> {
            blogService.plusHeart(blogId, user);
        });

        verify(blogHeartRepository, times(0)).save(any(BlogHeart.class));
    }

//    @DisplayName("블로그 좋아요 테스트 - 좋아요를 처음 누를 때 정상적으로 저장")
//    @Test
//    public void testPlusHeart_FirstTimeLike() {
//        // given
//        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
//                .willReturn(Optional.empty());
//        given(blogRepository.findById(blogId)).willReturn(Optional.of(blog));
//
//        // when
//        blogService.plusHeart(blogId, user);
//
//        // then
//        verify(blogHeartRepository, times(1)).save(any(BlogHeart.class));
//    }

    @DisplayName("블로그 좋아요 취소 테스트 - 좋아요를 하지 않은 상태에서 취소 시 예외 발생")
    @Test
    public void testMinusHeart_NoLike() {
        // given
        given(blogRepository.findById(blogId))
                .willReturn(Optional.of(blog));
        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(BlogNotFoundException.class, () -> {
            blogService.minusHeart(blogId, user);
        });

        verify(blogHeartRepository, times(0)).deleteByBlogIdAndUserId(blogId, blogId);
    }

    @DisplayName("블로그 좋아요 취소 테스트 - 좋아요를 눌렀을 때 정상적으로 취소")
    @Test
    public void testMinusHeart_LikeExists() {
        // given
        BlogHeart blogHeart = new BlogHeart(user, blog);

        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
                .willReturn(Optional.of(blogHeart));
        given(blogRepository.findById(any())).willReturn(Optional.of(blog));

        // when
        blogService.minusHeart(blogId, user);

        // then
        verify(blogHeartRepository, times(1)).deleteByBlogIdAndUserId(any(), any());
    }
} 