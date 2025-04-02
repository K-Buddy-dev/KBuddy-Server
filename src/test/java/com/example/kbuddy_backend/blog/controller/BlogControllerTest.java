package com.example.kbuddy_backend.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.service.BlogService;
import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.fixtures.BlogFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "123", roles = "USER")
public class BlogControllerTest extends WebMVCTest {

    @MockBean
    private BlogService blogService;

    @DisplayName("Blog 게시글 작성 테스트")
    @Test
    public void testCreateBlog() throws Exception {

        //given
        BlogSaveRequest blogSaveRequest = BlogFixtures.createBlogSaveRequest();
        BlogResponse blogResponse = BlogFixtures.createBlogResponse();
        User user = UserFixtures.createUser();

        given(blogService.saveBlog(any(BlogSaveRequest.class),eq(user))).willReturn(blogResponse);
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        mockMvc.perform(post("/kbuddy/v1/blog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blogSaveRequest)))
                .andExpect(status().isNoContent())
                .andDo(print());

        //then
        verify(blogService, times(1)).saveBlog(any(BlogSaveRequest.class),eq(user));
    }
}
