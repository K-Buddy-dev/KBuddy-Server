package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogHeartException;
import com.example.kbuddy_backend.blog.exception.BlogNotFoundException;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.fixtures.BlogFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

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
    private BlogHeartRepository blogHeartRepository;

    private User user;
    private Long blogId;
    private Blog blog;

    @BeforeEach
    void setUp() {
        user = UserFixtures.createUser();
        blogId = 1L;
        blog = BlogFixtures.createBlog();
    }

    @DisplayName("블로그 좋아요 테스트 - 이미 좋아요를 눌렀을 때 예외 발생")
    @Test
    public void testPlusHeart_AlreadyLiked() {
        // given
        BlogHeart existingBlogHeart = BlogFixtures.createHeart(blog, user);

        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
                .willReturn(Optional.of(existingBlogHeart));

        // when & then
        assertThrows(DuplicatedBlogHeartException.class, () -> {
            blogService.plusHeart(blogId, user);
        });

        verify(blogHeartRepository, times(0)).save(any(BlogHeart.class));
    }

    @DisplayName("블로그 좋아요 테스트 - 좋아요를 처음 누를 때 정상적으로 저장")
    @Test
    public void testPlusHeart_FirstTimeLike() {
        // given
        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
                .willReturn(Optional.empty());
        given(blogRepository.findById(blogId)).willReturn(Optional.of(blog));

        // when
        blogService.plusHeart(blogId, user);

        // then
        verify(blogHeartRepository, times(1)).save(any(BlogHeart.class));
    }

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

        verify(blogHeartRepository, times(0)).deleteByBlogIdAndUserId(any(), any());
    }

    @DisplayName("블로그 좋아요 취소 테스트 - 좋아요를 눌렀을 때 정상적으로 취소")
    @Test
    public void testMinusHeart_LikeExists() {
        // given
        BlogHeart blogHeart = BlogFixtures.createHeart(blog, user);

        given(blogHeartRepository.findByBlogIdAndUserId(any(), any()))
                .willReturn(Optional.of(blogHeart));
        given(blogRepository.findById(any())).willReturn(Optional.of(blog));

        // when
        blogService.minusHeart(blogId, user);

        // then
        verify(blogHeartRepository, times(1)).deleteByBlogIdAndUserId(any(), any());
    }
} 