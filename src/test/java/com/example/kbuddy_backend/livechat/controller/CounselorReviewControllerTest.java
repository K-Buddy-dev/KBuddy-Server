package com.example.kbuddy_backend.livechat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.fixtures.LiveChatFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.livechat.dto.request.CreateInquiryRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateReviewRequest;
import com.example.kbuddy_backend.livechat.service.CounselorInquiryService;
import com.example.kbuddy_backend.livechat.service.CounselorReviewService;
import com.example.kbuddy_backend.user.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

@WithMockUser(username = "123", roles = "USER")
public class CounselorReviewControllerTest extends WebMVCTest {

    @MockBean
    private CounselorReviewService counselorReviewService;

    @MockBean
    private CounselorInquiryService counselorInquiryService;

    @DisplayName("리뷰 작성 API 테스트 - 정상")
    @Test
    public void testCreateReview_Success() throws Exception {
        // given
        User user = UserFixtures.createUser();
        CreateReviewRequest request = LiveChatFixtures.createReviewRequest(1L);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        doNothing().when(counselorReviewService).createReview(any(User.class), any(CreateReviewRequest.class));

        // when & then
        mockMvc.perform(post("/kbuddy/v1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @DisplayName("리뷰 작성 API 테스트 - 완료되지 않은 예약")
    @Test
    public void testCreateReview_NotCompleted() throws Exception {
        // given
        User user = UserFixtures.createUser();
        CreateReviewRequest request = LiveChatFixtures.createReviewRequest(1L);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        doThrow(new IllegalStateException("완료된 예약만 리뷰를 작성할 수 있습니다"))
                .when(counselorReviewService).createReview(any(User.class), any(CreateReviewRequest.class));

        // when & then
        mockMvc.perform(post("/kbuddy/v1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError())
                .andDo(print());
    }

    @DisplayName("문의글 작성 API 테스트 - 정상")
    @Test
    public void testCreateInquiry_Success() throws Exception {
        // given
        User user = UserFixtures.createUser();
        CreateInquiryRequest request = LiveChatFixtures.createInquiryRequest();

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        doNothing().when(counselorInquiryService).createInquiry(any(User.class), eq(1L),
                any(CreateInquiryRequest.class));

        // when & then
        mockMvc.perform(post("/kbuddy/v1/counselor/1/inquiry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}
