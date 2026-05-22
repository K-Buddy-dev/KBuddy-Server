package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.dto.response.CounselorAvailabilityResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorDetailResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorListResponse;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.entity.CounselorReview;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorReviewRepository;
import com.example.kbuddy_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorProfileService {

        private final CounselorProfileRepository counselorProfileRepository;
        private final CounselorAvailabilityRepository availabilityRepository;
        private final CounselorReviewRepository reviewRepository;

        public CounselorListResponse getCounselors(String sort, Pageable pageable) {
                Page<CounselorProfile> profiles;

                if ("rating".equals(sort)) {
                        profiles = counselorProfileRepository.findAllOrderByRatingDesc(pageable);
                } else {
                        profiles = counselorProfileRepository.findAll(pageable);
                }

                List<CounselorListResponse.CounselorSummary> content = profiles.getContent().stream()
                                .map(this::toSummary)
                                .toList();

                return new CounselorListResponse(content, profiles.getTotalElements());
        }

        public CounselorDetailResponse getCounselor(Long counselorId) {
                CounselorProfile profile = counselorProfileRepository.findById(counselorId)
                                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: " + counselorId));

                List<CounselorReview> recentReviews = reviewRepository
                                .findTop3ByCounselorIdOrderByCreatedAtDesc(profile.getUser().getId());

                List<String> categories = profile.getCategories().stream()
                                .map(c -> c.getCategory().getDisplayName())
                                .toList();

                List<String> photoUrls = profile.getPhotos().stream()
                                .map(p -> p.getPhotoUrl())
                                .toList();

                CounselorDetailResponse.PromotionInfo promotionInfo = null;
                if (profile.getPromotion() != null) {
                        var promo = profile.getPromotion();
                        promotionInfo = new CounselorDetailResponse.PromotionInfo(
                                        promo.getPromotionalPrice(),
                                        promo.getPromotionSessionMinutes(),
                                        promo.getStartDate().toString(),
                                        promo.getEndDate().toString(),
                                        promo.isActive());
                }

                return new CounselorDetailResponse(
                                profile.getId().toString(),
                                profile.getUser().getFirstName() + " " + profile.getUser().getLastName(),
                                profile.getTitle(),
                                profile.getDetail(),
                                profile.getIntro(),
                                profile.getProfessionalBackground(),
                                profile.getCoverImageUrl(),
                                profile.getProofFileUrl(),
                                photoUrls,
                                categories,
                                profile.getRatingAvg(),
                                profile.getReviewCount(),
                                profile.getRegularPrice(),
                                profile.getSessionMinutes(),
                                profile.getTimezone(),
                                profile.getUser().getProfileImageUrl(),
                                promotionInfo,
                                recentReviews.stream()
                                                .map(r -> new CounselorDetailResponse.RecentReview(
                                                                r.getId(),
                                                                r.getCustomer().getFirstName(),
                                                                r.getRating(),
                                                                r.getComment(),
                                                                r.getCreatedAt().toString()))
                                                .toList());
        }

        public CounselorAvailabilityResponse getAvailability(Long counselorId, int year, int month) {
                YearMonth yearMonth = YearMonth.of(year, month);
                LocalDate startDate = yearMonth.atDay(1);
                LocalDate endDate = yearMonth.atEndOfMonth();

                var slots = availabilityRepository.findByCounselorIdAndSlotDateBetween(
                                counselorId, startDate, endDate);

                List<CounselorAvailabilityResponse.AvailabilitySlot> slotList = slots.stream()
                                .map(s -> new CounselorAvailabilityResponse.AvailabilitySlot(
                                                s.getId(),
                                                s.getSlotDate(),
                                                s.getSlotStartTime(),
                                                s.getStatus().name()))
                                .toList();

                return new CounselorAvailabilityResponse(slotList);
        }

        @Transactional
        public void registerCounselor(User user, String intro, Integer regularPrice, String timezone) {
                if (counselorProfileRepository.existsByUserId(user.getId())) {
                        throw new IllegalStateException("이미 상담사로 등록된 사용자입니다");
                }

                CounselorProfile profile = CounselorProfile.builder()
                                .user(user)
                                .intro(intro)
                                .regularPrice(regularPrice)
                                .timezone(timezone)
                                .build();

                counselorProfileRepository.save(profile);
        }

        private CounselorListResponse.CounselorSummary toSummary(CounselorProfile profile) {
                List<String> categories = profile.getCategories().stream()
                                .map(c -> c.getCategory().getDisplayName())
                                .toList();

                return new CounselorListResponse.CounselorSummary(
                                profile.getId().toString(),
                                profile.getUser().getFirstName() + " " + profile.getUser().getLastName(),
                                profile.getTitle(),
                                categories,
                                profile.getRatingAvg(),
                                profile.getReviewCount(),
                                profile.getRegularPrice(),
                                profile.getSessionMinutes(),
                                profile.getCoverImageUrl(),
                                profile.getPromotion() != null && profile.getPromotion().isActive());
        }
}
