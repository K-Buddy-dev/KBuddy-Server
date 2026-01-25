package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.response.BookingReserveResponse;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingService {

    private static final int PAYMENT_TIMEOUT_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final CounselorAvailabilityRepository availabilityRepository;

    @Transactional
    public BookingReserveResponse reserve(User customer, BookingReserveRequest request) {
        try {
            // 낙관적 락으로 슬롯 조회
            CounselorAvailability slot = availabilityRepository.findByIdWithLock(request.availabilityId())
                    .orElseThrow(() -> new IllegalArgumentException("가용 시간 슬롯을 찾을 수 없습니다"));

            // 이미 예약된 슬롯인지 확인
            if (slot.isBooked()) {
                throw new IllegalStateException("이미 예약된 시간대입니다");
            }

            // 슬롯 예약 처리
            slot.book();

            // 상담사 정보 조회
            CounselorProfile counselorProfile = slot.getCounselor();
            User counselor = counselorProfile.getUser();

            // Booking 생성
            Booking booking = Booking.builder()
                    .customer(customer)
                    .counselor(counselor)
                    .availability(slot)
                    .totalPrice(counselorProfile.getHourlyRate())
                    .build();

            bookingRepository.save(booking);

            // 결제 만료 시간 계산
            Instant holdExpiresAt = Instant.now().plusSeconds(PAYMENT_TIMEOUT_MINUTES * 60L);

            return new BookingReserveResponse(booking.getId(), booking.getStatus().name(), holdExpiresAt);

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("다른 사용자가 방금 예약한 시간대입니다. 다른 시간을 선택해주세요.");
        }
    }

    @Transactional
    public void confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다"));

        booking.confirmPayment();
    }

    @Transactional
    public void completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다"));

        booking.complete();
    }

    @Transactional
    public void cancelBooking(User user, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다"));

        // 본인 예약인지 확인
        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new IllegalArgumentException("이 예약을 취소할 권한이 없습니다");
        }

        booking.cancel();
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);

        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(
                BookingStatus.PENDING, expireTime);

        for (Booking booking : expiredBookings) {
            try {
                booking.cancel();
                log.info("만료된 예약 자동 취소: {}", booking.getId());
            } catch (Exception e) {
                log.error("만료된 예약 취소 실패: {}", booking.getId(), e);
            }
        }
    }
}
