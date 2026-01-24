package com.example.kbuddy_backend.livechat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.common.config.DataInitializer;
import com.example.kbuddy_backend.fixtures.LiveChatFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.response.BookingReserveResponse;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

public class BookingServiceTest extends IntegrationTest {

    @Autowired
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private CounselorAvailabilityRepository availabilityRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DataInitializer dataInitializer;

    private User customer;
    private User counselor;
    private CounselorProfile counselorProfile;
    private CounselorAvailability availability;

    @BeforeEach
    void setUp() {
        customer = UserFixtures.createUser();
        counselor = UserFixtures.createUser();
        counselorProfile = LiveChatFixtures.createCounselorProfile(counselor);
        availability = LiveChatFixtures.createAvailability(counselorProfile);
    }

    @DisplayName("예약 선점 테스트 - 정상적으로 예약 생성")
    @Test
    public void testReserve_Success() {
        // given
        BookingReserveRequest request = LiveChatFixtures.createBookingReserveRequest(1L);

        given(availabilityRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(availability));
        given(bookingRepository.save(any(Booking.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        BookingReserveResponse response = bookingService.reserve(customer, request);

        // then
        assertNotNull(response);
        assertEquals(BookingStatus.PENDING.name(), response.status());
        assertNotNull(response.holdExpiresAt());
        assertTrue(availability.isBooked());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @DisplayName("예약 선점 테스트 - 이미 예약된 슬롯일 때 예외 발생")
    @Test
    public void testReserve_AlreadyBooked() {
        // given
        BookingReserveRequest request = LiveChatFixtures.createBookingReserveRequest(1L);
        availability.book(); // 이미 예약 처리

        given(availabilityRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(availability));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            bookingService.reserve(customer, request);
        });

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @DisplayName("예약 선점 테스트 - 존재하지 않는 슬롯일 때 예외 발생")
    @Test
    public void testReserve_SlotNotFound() {
        // given
        BookingReserveRequest request = LiveChatFixtures.createBookingReserveRequest(999L);

        given(availabilityRepository.findByIdWithLock(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.reserve(customer, request);
        });

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @DisplayName("결제 확인 테스트 - PENDING에서 PAID로 상태 변경")
    @Test
    public void testConfirmPayment_Success() {
        // given
        Booking booking = LiveChatFixtures.createBooking(customer, counselor, availability);

        given(bookingRepository.findById(1L))
                .willReturn(Optional.of(booking));

        // when
        bookingService.confirmPayment(1L);

        // then
        assertEquals(BookingStatus.PAID, booking.getStatus());
    }

    @DisplayName("상담 완료 테스트 - PAID에서 COMPLETED로 상태 변경")
    @Test
    public void testCompleteBooking_Success() {
        // given
        Booking booking = LiveChatFixtures.createBooking(customer, counselor, availability);
        booking.confirmPayment(); // PENDING -> PAID

        given(bookingRepository.findById(1L))
                .willReturn(Optional.of(booking));

        // when
        bookingService.completeBooking(1L);

        // then
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }
}
