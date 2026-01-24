package com.example.kbuddy_backend.fixtures;

import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.constant.Specialty;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateInquiryRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateReviewRequest;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.entity.CounselorReview;
import com.example.kbuddy_backend.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class LiveChatFixtures {

    // Counselor Profile
    public static CounselorProfile createCounselorProfile(User user) {
        return CounselorProfile.builder()
                .user(user)
                .intro("안녕하세요, 전문 상담사입니다.")
                .specialty(Specialty.UNIVERSITY)
                .hourlyRate(50000)
                .build();
    }

    // Counselor Availability
    public static CounselorAvailability createAvailability(CounselorProfile profile) {
        return CounselorAvailability.builder()
                .counselor(profile)
                .availableDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(14, 0))
                .build();
    }

    public static CounselorAvailability createAvailability(CounselorProfile profile, LocalDate date, LocalTime time) {
        return CounselorAvailability.builder()
                .counselor(profile)
                .availableDate(date)
                .startTime(time)
                .build();
    }

    // Booking
    public static Booking createBooking(User customer, User counselor, CounselorAvailability availability) {
        return Booking.builder()
                .customer(customer)
                .counselor(counselor)
                .availability(availability)
                .totalPrice(50000)
                .build();
    }

    // Counselor Review
    public static CounselorReview createReview(Booking booking, User customer, User counselor) {
        return CounselorReview.builder()
                .booking(booking)
                .customer(customer)
                .counselor(counselor)
                .rating(5)
                .comment("정말 좋은 상담이었습니다!")
                .build();
    }

    // Counselor Inquiry
    public static CounselorInquiry createInquiry(User counselor, User writer) {
        return CounselorInquiry.builder()
                .counselor(counselor)
                .writer(writer)
                .title("상담 문의드립니다")
                .content("상담 가능 시간이 어떻게 되나요?")
                .isSecret(false)
                .build();
    }

    public static CounselorInquiry createSecretInquiry(User counselor, User writer) {
        return CounselorInquiry.builder()
                .counselor(counselor)
                .writer(writer)
                .title("비밀 문의")
                .content("비밀 내용입니다.")
                .isSecret(true)
                .build();
    }

    // Request DTOs
    public static BookingReserveRequest createBookingReserveRequest(Long availabilityId) {
        return new BookingReserveRequest("1", availabilityId);
    }

    public static CreateReviewRequest createReviewRequest(Long bookingId) {
        return new CreateReviewRequest(bookingId, 5, "훌륭한 상담이었습니다!");
    }

    public static CreateInquiryRequest createInquiryRequest() {
        return new CreateInquiryRequest("상담 문의", "상담 가능 시간이 언제인가요?", false);
    }
}
