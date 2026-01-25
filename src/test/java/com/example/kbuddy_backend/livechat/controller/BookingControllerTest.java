package com.example.kbuddy_backend.livechat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.fixtures.LiveChatFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.response.BookingReserveResponse;
import com.example.kbuddy_backend.livechat.service.BookingService;
import com.example.kbuddy_backend.user.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.Optional;

@WithMockUser(username = "123", roles = "USER")
public class BookingControllerTest extends WebMVCTest {

    @MockBean
    private BookingService bookingService;

    @DisplayName("예약 선점 API 테스트 - 정상")
    @Test
    public void testReserveBooking_Success() throws Exception {
        // given
        User user = UserFixtures.createUser();
        BookingReserveRequest request = LiveChatFixtures.createBookingReserveRequest(1L);
        BookingReserveResponse response = new BookingReserveResponse(
                1L, BookingStatus.PENDING.name(), Instant.now().plusSeconds(600));

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookingService.reserve(any(User.class), any(BookingReserveRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/kbuddy/v1/booking/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("예약 선점 API 테스트 - 이미 예약된 슬롯")
    @Test
    public void testReserveBooking_AlreadyBooked() throws Exception {
        // given
        User user = UserFixtures.createUser();
        BookingReserveRequest request = LiveChatFixtures.createBookingReserveRequest(1L);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookingService.reserve(any(User.class), any(BookingReserveRequest.class)))
                .willThrow(new IllegalStateException("이미 예약된 시간대입니다"));

        // when & then
        mockMvc.perform(post("/kbuddy/v1/booking/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError())
                .andDo(print());
    }
}
