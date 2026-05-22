package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.response.BookingListResponse;
import com.example.kbuddy_backend.livechat.dto.response.BookingReserveResponse;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.entity.BookingSlot;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingService {

    private static final int PAYMENT_TIMEOUT_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final CounselorAvailabilityRepository availabilityRepository;
    private final CounselorProfileRepository counselorProfileRepository;

    @Transactional
    public BookingReserveResponse reserve(User customer, BookingReserveRequest request) {
        try {
            // 낙관적 락으로 모든 슬롯 조회
            List<CounselorAvailability> slots = availabilityRepository.findAllByIdWithLock(request.slotIds());

            // 요청한 슬롯 수와 조회된 슬롯 수 일치 확인
            if (slots.size() != request.slotIds().size()) {
                throw new IllegalArgumentException("일부 슬롯을 찾을 수 없습니다");
            }

            // 모든 슬롯이 같은 상담사의 것인지 확인
            CounselorProfile counselorProfile = slots.get(0).getCounselor();
            boolean allSameCounselor = slots.stream()
                    .allMatch(s -> s.getCounselor().getId().equals(counselorProfile.getId()));
            if (!allSameCounselor) {
                throw new IllegalArgumentException("모든 슬롯은 같은 상담사의 것이어야 합니다");
            }

            // 모든 슬롯이 예약 가능한지 확인 후 예약 처리
            for (CounselorAvailability slot : slots) {
                slot.book();
            }

            // 시간순 정렬하여 시작/종료 시간 계산
            slots.sort(Comparator.comparing(CounselorAvailability::getSlotDate)
                    .thenComparing(CounselorAvailability::getSlotStartTime));

            CounselorAvailability firstSlot = slots.get(0);
            CounselorAvailability lastSlot = slots.get(slots.size() - 1);

            LocalDateTime bookingStartUtc = LocalDateTime.of(firstSlot.getSlotDate(), firstSlot.getSlotStartTime());
            LocalDateTime bookingEndUtc = LocalDateTime.of(lastSlot.getSlotDate(),
                    lastSlot.getSlotStartTime().plusMinutes(30));

            int slotCount = slots.size();
            int totalPrice = slotCount * counselorProfile.getRegularPrice();

            User counselor = counselorProfile.getUser();

            // Booking 생성
            Booking booking = Booking.builder()
                    .customer(customer)
                    .counselor(counselor)
                    .bookingStartUtc(bookingStartUtc)
                    .bookingEndUtc(bookingEndUtc)
                    .slotCount(slotCount)
                    .totalPrice(totalPrice)
                    .topic(request.topic())
                    .memo(request.memo())
                    .build();

            bookingRepository.save(booking);

            // BookingSlot 매핑 생성
            for (CounselorAvailability slot : slots) {
                BookingSlot bookingSlot = BookingSlot.builder()
                        .booking(booking)
                        .availability(slot)
                        .build();
                booking.addBookingSlot(bookingSlot);
            }

            // 결제 만료 시간 계산
            Instant holdExpiresAt = Instant.now().plusSeconds(PAYMENT_TIMEOUT_MINUTES * 60L);

            return new BookingReserveResponse(
                    booking.getId(),
                    booking.getStatus().name(),
                    slotCount,
                    totalPrice,
                    bookingStartUtc,
                    bookingEndUtc,
                    holdExpiresAt);

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

    public BookingListResponse getMyBookings(User customer, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByCustomer(customer, pageable);
        List<BookingListResponse.BookingSummary> content = bookings.getContent().stream()
                .map(b -> {
                    CounselorProfile profile = counselorProfileRepository
                            .findByUserId(b.getCounselor().getId()).orElse(null);
                    String name = b.getCounselor().getFirstName() + " " + b.getCounselor().getLastName();
                    String coverUrl = profile != null ? profile.getCoverImageUrl() : null;
                    return new BookingListResponse.BookingSummary(
                            b.getId(), name, coverUrl, b.getTopic(),
                            b.getStatus().name(), b.getTotalPrice(),
                            b.getBookingStartUtc(), b.getBookingEndUtc());
                }).toList();
        return new BookingListResponse(content, bookings.getTotalElements());
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
