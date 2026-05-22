package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.constant.Category;
import com.example.kbuddy_backend.livechat.dto.request.RegisterCounselorRequest;
import com.example.kbuddy_backend.livechat.dto.request.UpdateCounselorRequest;
import com.example.kbuddy_backend.livechat.dto.response.CounselorAvailabilityResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorDetailResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorListResponse;
import com.example.kbuddy_backend.livechat.entity.CounselorCategory;
import com.example.kbuddy_backend.livechat.entity.CounselorPhoto;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.entity.CounselorPromotion;
import com.example.kbuddy_backend.livechat.entity.CounselorReview;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorCategoryRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorPhotoRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorPromotionRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorReviewRepository;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
        private final CounselorCategoryRepository categoryRepository;
        private final CounselorPromotionRepository promotionRepository;
        private final CounselorPhotoRepository photoRepository;
        private final S3Service s3Service;

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
        public void registerCounselor(User user, RegisterCounselorRequest request,
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
                        categoryRepository.deleteByCounselorId(profile.getId());
                        saveCategories(profile, request.categories());
                }

                if (photos != null && !photos.isEmpty()) {
                        photoRepository.deleteByCounselorId(profile.getId());
                        savePhotos(profile, photos);
                }

                if (request.promotionalPrice() != null) {
                        promotionRepository.deleteByCounselorId(profile.getId());
                        savePromotion(profile, request.promotionalPrice(),
                                        request.promotionSessionMinutes(), request.promotionStartDate(), request.promotionEndDate());
                }
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
