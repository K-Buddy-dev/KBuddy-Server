package com.example.kbuddy_backend.livechat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.common.config.DataInitializer;
import com.example.kbuddy_backend.fixtures.LiveChatFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.livechat.dto.request.CreateReviewRequest;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorReviewRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

public class CounselorReviewServiceTest extends IntegrationTest {

    @Autowired
    private CounselorReviewService counselorReviewService;

    @MockBean
    private CounselorReviewRepository reviewRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private CounselorProfileRepository counselorProfileRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DataInitializer dataInitializer;

    private User customer;
    private User counselor;
    private CounselorProfile counselorProfile;
    private CounselorAvailability availability;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = UserFixtures.createUser();
        counselor = UserFixtures.createUser();
        counselorProfile = LiveChatFixtures.createCounselorProfile(counselor);
        availability = LiveChatFixtures.createAvailability(counselorProfile);
        booking = LiveChatFixtures.createBooking(customer, counselor, availability);
    }

    @DisplayName("예약 존재하지 않을 때 리뷰 작성 시 예외 발생")
    @Test
    public void testCreateReview_BookingNotFound() {
        // given
        CreateReviewRequest request = LiveChatFixtures.createReviewRequest(999L);

        given(bookingRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            counselorReviewService.createReview(customer, request);
        });
    }
}
