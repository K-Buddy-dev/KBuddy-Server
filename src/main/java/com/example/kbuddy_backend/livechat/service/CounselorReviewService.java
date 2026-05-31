package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.dto.request.CreateReviewRequest;
import com.example.kbuddy_backend.livechat.dto.response.ReviewListResponse;
import com.example.kbuddy_backend.user.util.UserNameUtils;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.entity.CounselorReview;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorReviewRepository;
import com.example.kbuddy_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorReviewService {

    private final CounselorReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final CounselorProfileRepository counselorProfileRepository;

    public ReviewListResponse getReviews(String counselorUuid, Pageable pageable) {
        CounselorProfile profile = counselorProfileRepository.findByUuid(UUID.fromString(counselorUuid))
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: " + counselorUuid));

        Page<CounselorReview> page = reviewRepository.findByCounselorId(profile.getUser().getId(), pageable);

        var items = page.getContent().stream()
                .map(r -> new ReviewListResponse.ReviewItem(
                        r.getId(),
                        UserNameUtils.fullName(r.getCustomer()),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt()))
                .toList();

        return new ReviewListResponse(items, page.getTotalElements());
    }

    @Transactional
    public void createReview(User customer, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다"));

        // 본인 예약인지 확인
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("이 예약에 대한 리뷰 작성 권한이 없습니다");
        }

        // 상담 완료 상태인지 확인
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("완료된 예약만 리뷰를 작성할 수 있습니다");
        }

        // 이미 리뷰가 있는지 확인
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("이미 이 예약에 대한 리뷰가 존재합니다");
        }

        CounselorReview review = CounselorReview.builder()
                .booking(booking)
                .customer(customer)
                .counselor(booking.getCounselor())
                .rating(request.rating())
                .comment(request.comment())
                .build();

        reviewRepository.save(review);

        // 상담사 평균 평점 업데이트
        updateCounselorRating(booking.getCounselor().getId());
    }

    private void updateCounselorRating(Long counselorUserId) {
        CounselorProfile profile = counselorProfileRepository.findByUserId(counselorUserId)
                .orElseThrow(() -> new IllegalArgumentException("상담사 프로필을 찾을 수 없습니다"));

        BigDecimal avgRating = reviewRepository.calculateAverageRating(counselorUserId);

        if (avgRating != null) {
            profile.updateRatingAvg(avgRating);
        }

        profile.incrementReviewCount();
    }
}
