package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.constant.Category;
import com.example.kbuddy_backend.livechat.constant.SlotStatus;
import com.example.kbuddy_backend.livechat.dto.request.SlotRequest;
import com.example.kbuddy_backend.livechat.dto.request.RegisterCounselorRequest;
import com.example.kbuddy_backend.livechat.dto.request.UpdateCounselorRequest;
import com.example.kbuddy_backend.livechat.dto.response.CounselorAvailabilityResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorDetailResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorListResponse;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorCategory;
import com.example.kbuddy_backend.livechat.entity.CounselorPhoto;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.entity.CounselorPromotion;
import com.example.kbuddy_backend.livechat.entity.CounselorReview;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorCategoryRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorInquiryRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorPhotoRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorPromotionRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorReviewRepository;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.util.UserNameUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorProfileService {

        private final CounselorProfileRepository counselorProfileRepository;
        private final CounselorAvailabilityRepository availabilityRepository;
        private final CounselorReviewRepository reviewRepository;
        private final CounselorCategoryRepository categoryRepository;
        private final CounselorPromotionRepository promotionRepository;
        private final CounselorPhotoRepository photoRepository;
        private final CounselorInquiryRepository inquiryRepository;
        private final S3Service s3Service;

        @PersistenceContext
        private EntityManager entityManager;

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

        public CounselorDetailResponse getMyProfile(User user) {
                CounselorProfile profile = counselorProfileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("상담사로 등록되지 않은 사용자입니다"));
                return toCounselorDetailResponse(profile);
        }

        public CounselorDetailResponse getCounselor(String counselorUuid) {
                CounselorProfile profile = counselorProfileRepository.findByUuid(UUID.fromString(counselorUuid))
                                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: " + counselorUuid));
                return toCounselorDetailResponse(profile);
        }

        private CounselorDetailResponse toCounselorDetailResponse(CounselorProfile profile) {
                List<CounselorReview> recentReviews = reviewRepository
                                .findTop3ByCounselorIdOrderByCreatedAtDesc(profile.getUser().getId());

                List<CounselorDetailResponse.RecentInquiry> recentInquiries = inquiryRepository
                                .findTop5ByCounselorIdAndIsSecretFalseOrderByCreatedDateDesc(profile.getUser().getId())
                                .stream()
                                .map(i -> new CounselorDetailResponse.RecentInquiry(
                                                i.getId(),
                                                i.getTitle(),
                                                UserNameUtils.fullName(i.getWriter()),
                                                i.isSecret(),
                                                i.getCreatedDate()))
                                .toList();

                List<String> categories = profile.getCategories().stream()
                                .map(c -> c.getCategory().getDisplayName())
                                .toList();

                List<String> photoUrls = profile.getPhotos().stream()
                                .map(CounselorPhoto::getPhotoUrl)
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
                                profile.getUuid().toString(),
                                UserNameUtils.fullName(profile.getUser()),
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
                                                                UserNameUtils.fullName(r.getCustomer()),
                                                                r.getRating(),
                                                                r.getComment(),
                                                                r.getCreatedAt()))
                                                .toList(),
                                recentInquiries);
        }

        public CounselorAvailabilityResponse getAvailability(String counselorUuid, int year, int month) {
                CounselorProfile profile = counselorProfileRepository.findByUuid(UUID.fromString(counselorUuid))
                                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: " + counselorUuid));

                YearMonth yearMonth = YearMonth.of(year, month);
                LocalDateTime startUtc = yearMonth.atDay(1).minusDays(1).atStartOfDay();
                LocalDateTime endUtc = yearMonth.atEndOfMonth().plusDays(1).atTime(23, 59, 59);

                var slots = availabilityRepository.findByCounselorIdAndSlotStartUtcBetween(
                                profile.getId(), startUtc, endUtc);

                List<CounselorAvailabilityResponse.AvailabilitySlot> slotList = slots.stream()
                                .map(s -> new CounselorAvailabilityResponse.AvailabilitySlot(
                                                s.getId(),
                                                s.getSlotStartUtc().toInstant(java.time.ZoneOffset.UTC),
                                                s.getStatus().name()))
                                .toList();

                return new CounselorAvailabilityResponse(slotList);
        }

        @Transactional
        public String registerCounselor(User user, RegisterCounselorRequest request,
                                       MultipartFile coverImage, MultipartFile proofFile,
                                       List<MultipartFile> photos) {
                if (counselorProfileRepository.existsByUserId(user.getId())) {
                        throw new IllegalStateException("이미 상담사로 등록된 사용자입니다");
                }

                String coverImageUrl = uploadFile(coverImage, "counselor/cover");
                String proofFileUrl = uploadFile(proofFile, "counselor/proof");

                CounselorProfile profile = CounselorProfile.builder()
                                .user(user)
                                .title(request.title())
                                .detail(request.detail())
                                .intro(request.intro())
                                .professionalBackground(request.professionalBackground())
                                .coverImageUrl(coverImageUrl)
                                .proofFileUrl(proofFileUrl)
                                .regularPrice(request.regularPrice())
                                .sessionMinutes(request.sessionMinutes())
                                .timezone(request.timezone())
                                .build();

                counselorProfileRepository.save(profile);
                saveCategories(profile, request.categories());

                if (photos != null && !photos.isEmpty()) {
                        savePhotos(profile, photos);
                }

                if (request.promotionalPrice() != null) {
                        savePromotion(profile, request.promotionalPrice(),
                                        request.promotionSessionMinutes(), request.promotionStartDate(), request.promotionEndDate());
                }

                if (request.slots() != null && !request.slots().isEmpty()) {
                        saveSlots(profile, request.slots());
                }

                return profile.getUuid().toString();
        }

        @Transactional
        public void updateCounselor(User user, UpdateCounselorRequest request,
                                      MultipartFile coverImage, MultipartFile proofFile,
                                      List<MultipartFile> photos) {
                CounselorProfile profile = counselorProfileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("상담사로 등록되지 않은 사용자입니다"));

                String coverImageUrl = coverImage != null && !coverImage.isEmpty()
                                ? uploadFile(coverImage, "counselor/cover") : null;
                String proofFileUrl = proofFile != null && !proofFile.isEmpty()
                                ? uploadFile(proofFile, "counselor/proof") : null;

                profile.updateProfile(request.title(), request.detail(), request.intro(),
                                request.professionalBackground(), coverImageUrl, proofFileUrl,
                                request.regularPrice(), request.sessionMinutes(), request.timezone());

                if (request.categories() != null && !request.categories().isEmpty()) {
                        profile.getCategories().clear();
                        entityManager.flush(); // orphan DELETE를 INSERT 전에 강제 실행
                        List<CounselorCategory> newCategories = request.categories().stream()
                                        .map(name -> {
                                                try {
                                                        return Category.valueOf(name);
                                                } catch (IllegalArgumentException e) {
                                                        throw new IllegalArgumentException("유효하지 않은 카테고리: " + name);
                                                }
                                        })
                                        .map(cat -> CounselorCategory.builder().counselor(profile).category(cat).build())
                                        .toList();
                        profile.getCategories().addAll(newCategories);
                }

                if (request.existingPhotoUrls() != null) {
                        List<String> newUrls = new java.util.ArrayList<>();
                        if (photos != null && !photos.isEmpty()) {
                                for (MultipartFile photo : photos) {
                                        newUrls.add(s3Service.saveFileWithUUID(photo, "counselor/photos").s3ImageUrl());
                                }
                        }
                        List<String> finalUrls = new java.util.ArrayList<>(request.existingPhotoUrls());
                        finalUrls.addAll(newUrls);

                        profile.getPhotos().clear();
                        entityManager.flush();
                        for (int i = 0; i < finalUrls.size(); i++) {
                                profile.getPhotos().add(CounselorPhoto.builder()
                                                .counselor(profile).photoUrl(finalUrls.get(i)).sortOrder(i).build());
                        }
                }

                if (request.promotionalPrice() != null) {
                        if (profile.getPromotion() != null) {
                                profile.getPromotion().update(
                                                request.promotionalPrice(),
                                                request.promotionSessionMinutes(),
                                                request.promotionStartDate(),
                                                request.promotionEndDate());
                        } else {
                                savePromotion(profile, request.promotionalPrice(),
                                                request.promotionSessionMinutes(), request.promotionStartDate(), request.promotionEndDate());
                        }
                }

                if (request.slots() != null) {
                        availabilityRepository.deleteByCounselorIdAndStatus(profile.getId(), SlotStatus.AVAILABLE);
                        if (!request.slots().isEmpty()) {
                                saveSlots(profile, request.slots());
                        }
                }
        }

        @Transactional
        public void deleteCounselor(User user) {
                CounselorProfile profile = counselorProfileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("상담사로 등록되지 않은 사용자입니다"));
                profile.markDeleted();
        }

        private String uploadFile(MultipartFile file, String folder) {
                if (file == null || file.isEmpty()) return null;
                if (!s3Service.checkImageFile(file)) {
                        throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
                }
                return s3Service.saveFileWithUUID(file, folder).s3ImageUrl();
        }

        private void saveCategories(CounselorProfile profile, List<String> categoryNames) {
                categoryNames.stream()
                                .map(name -> {
                                        try {
                                                return Category.valueOf(name);
                                        } catch (IllegalArgumentException e) {
                                                throw new IllegalArgumentException("유효하지 않은 카테고리: " + name);
                                        }
                                })
                                .map(cat -> CounselorCategory.builder().counselor(profile).category(cat).build())
                                .forEach(categoryRepository::save);
        }

        private void savePhotos(CounselorProfile profile, List<MultipartFile> photos) {
                for (int i = 0; i < photos.size(); i++) {
                        String url = s3Service.saveFileWithUUID(photos.get(i), "counselor/photos").s3ImageUrl();
                        photoRepository.save(CounselorPhoto.builder()
                                        .counselor(profile).photoUrl(url).sortOrder(i).build());
                }
        }

        private void savePromotion(CounselorProfile profile, Integer promotionalPrice,
                                   Integer promotionSessionMinutes,
                                   LocalDate startDate, LocalDate endDate) {
                CounselorPromotion promotion = CounselorPromotion.builder()
                                .counselor(profile)
                                .promotionalPrice(promotionalPrice)
                                .promotionSessionMinutes(promotionSessionMinutes)
                                .startDate(startDate)
                                .endDate(endDate)
                                .build();
                promotionRepository.save(promotion);
        }

        private void saveSlots(CounselorProfile profile, List<SlotRequest> slotRequests) {
                for (SlotRequest req : slotRequests) {
                        for (LocalTime time : req.times()) {
                                LocalDateTime slotStartUtc = toUtc(req.date(), time, profile.getTimezone());
                                if (!availabilityRepository.existsByCounselorAndSlotStartUtc(profile, slotStartUtc)) {
                                        availabilityRepository.save(CounselorAvailability.builder()
                                                        .counselor(profile)
                                                        .slotStartUtc(slotStartUtc)
                                                        .build());
                                }
                        }
                }
        }

        private LocalDateTime toUtc(LocalDate date, LocalTime time, String timezone) {
                ZoneId zone = ZoneId.of(timezone != null ? timezone : "Asia/Seoul");
                return ZonedDateTime.of(date, time, zone)
                                .withZoneSameInstant(ZoneOffset.UTC)
                                .toLocalDateTime();
        }

        private CounselorListResponse.CounselorSummary toSummary(CounselorProfile profile) {
                List<String> categories = profile.getCategories().stream()
                                .map(c -> c.getCategory().getDisplayName())
                                .toList();

                return new CounselorListResponse.CounselorSummary(
                                profile.getUuid().toString(),
                                UserNameUtils.fullName(profile.getUser()),
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
