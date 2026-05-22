# Counselor Profile API Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** UI 스크린(3단계 상담사 프로필 생성 페이지)에 맞게 엔티티를 재설계하고, 프로필 등록/수정·가용시간 관리·예약 관리 API를 완성한다.

**Architecture:** `CounselorProfile` 엔티티에 title·detail·professionalBackground·coverImageUrl·proofFileUrl·sessionMinutes·reviewCount 필드를 추가한다. 다중 카테고리는 `CounselorCategory` 조인 테이블로, 추가 사진은 `CounselorPhoto` 엔티티로 분리한다. 프로모션은 `CounselorPromotion` 엔티티로 분리한다. 프로필·예약 관련 쓰기 API는 모두 컨트롤러까지 노출한다.

**Tech Stack:** Spring Boot 3, Spring Data JPA, PostgreSQL, Lombok, SpringDoc OpenAPI, AWS S3(Naver Cloud)

---

## 현재 상태 요약

| 영역 | 상태 |
|------|------|
| 상담사 목록/상세 조회 API | ✅ 완료 |
| 가용시간 조회 API | ✅ 완료 |
| 가용시간 단일/일괄(하루) 추가 API | ✅ 완료 |
| 상담사 프로필 등록 API | ❌ 서비스만 있음, 컨트롤러 없음 |
| 상담사 프로필 수정 API | ❌ 엔티티 메서드만 있음 |
| 가용시간 삭제 API | ❌ 서비스만 있음, 컨트롤러 없음 |
| 가용시간 날짜범위 일괄 등록 | ❌ 미구현 |
| 예약 결제확인/완료/취소 API | ❌ 서비스만 있음, 컨트롤러 없음 |
| 내 예약 목록 API | ❌ 미구현 |
| CounselorProfile 엔티티 UI 반영 | ❌ title, professionalBackground 등 다수 누락 |
| 다중 카테고리 | ❌ 단수 Specialty enum만 존재 |
| 프로모션 | ❌ 미구현 |

---

## Task 1: Category enum 교체 (Specialty → Category)

UI 3번째 스크린의 카테고리 목록(Restaurant, Cafe/Dessert …)으로 교체한다.
현재 `Specialty`를 사용하는 곳은 `CounselorProfile`, `CounselorProfileService`, `CounselorProfileRepository`, `CounselorController`이다.

**Files:**
- Create: `src/main/java/com/example/kbuddy_backend/livechat/constant/Category.java`
- Delete: `src/main/java/com/example/kbuddy_backend/livechat/constant/Specialty.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorProfile.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/repository/CounselorProfileRepository.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/service/CounselorProfileService.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/controller/CounselorController.java`

**Step 1: Category enum 생성**

```java
// src/main/java/com/example/kbuddy_backend/livechat/constant/Category.java
package com.example.kbuddy_backend.livechat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    RESTAURANT("Restaurant"),
    CAFE_DESSERT("Cafe/Dessert"),
    SHOPPING("Shopping"),
    ATTRACTION("Attraction"),
    LODGING("Lodging"),
    NATURE("Nature"),
    ART("Art"),
    BEAUTY_SPA("Beauty/Spa"),
    TRANSPORTATION("Transportation"),
    HEALTH("Health"),
    DAILY_LIFE("Daily Life"),
    OTHERS("Others");

    private final String displayName;
}
```

**Step 2: CounselorProfile에서 Specialty 참조 제거**

`CounselorProfile.java`에서 `Specialty specialty` 필드를 일단 삭제한다 (Task 3에서 다중 카테고리로 대체).

**Step 3: Specialty 참조하는 모든 파일에서 import·사용 제거**

`CounselorProfileRepository`, `CounselorProfileService`, `CounselorController`에서 `Specialty` 파라미터를 제거하고 컴파일 에러를 모두 해소한다.

**Step 4: 빌드 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -p
git commit -m "refactor: Specialty enum을 Category enum으로 교체"
```

---

## Task 2: CounselorProfile 엔티티 필드 추가

UI에서 요구하는 필드를 모두 추가한다.

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorProfile.java`

**Step 1: 필드 추가**

현재 엔티티에 아래 필드를 추가한다.

```java
@Column(length = 100, nullable = false)
private String title;                  // Title of listing (UI Step 1)

@Column(columnDefinition = "TEXT")
private String detail;                 // Detail (UI Step 1) — 기존 intro와 구분

@Column(columnDefinition = "TEXT")
private String professionalBackground; // Professional Background (UI Step 1)

private String coverImageUrl;          // 커버 이미지 (UI Step 1, 필수)

private String proofFileUrl;           // Add file of proof (UI Step 1)

@Column(nullable = false)
private Integer sessionMinutes = 30;   // 상담 세션 시간(분), 기본 30분

@Column(nullable = false)
private Integer regularPrice;          // Regular price (UI Step 2) — slotRate 대체

@Column(nullable = false)
private Integer reviewCount = 0;       // N+1 방지용 카운터 캐싱
```

**Step 2: 기존 `slotRate` 필드 제거**

`slotRate`를 `regularPrice`로 대체한다. `slotRate`를 참조하는 곳:
- `CounselorProfileService.toSummary()` → `regularPrice` 사용으로 변경
- `CounselorProfileService.getCounselor()` → `regularPrice` 사용으로 변경
- `BookingService.reserve()` → `getSlotRate()` → `getRegularPrice()` 변경
- `CounselorDetailResponse`, `CounselorListResponse` → `slotRate` → `regularPrice`

**Step 3: Builder 업데이트**

```java
@Builder
public CounselorProfile(User user, String title, String detail, String intro,
                         String professionalBackground, String coverImageUrl,
                         String proofFileUrl, Integer regularPrice,
                         Integer sessionMinutes, String timezone) {
    this.user = user;
    this.title = title;
    this.detail = detail;
    this.intro = intro;
    this.professionalBackground = professionalBackground;
    this.coverImageUrl = coverImageUrl;
    this.proofFileUrl = proofFileUrl;
    this.regularPrice = regularPrice;
    this.sessionMinutes = sessionMinutes != null ? sessionMinutes : 30;
    this.timezone = timezone != null ? timezone : "UTC";
    this.ratingAvg = BigDecimal.ZERO;
    this.reviewCount = 0;
}
```

**Step 4: updateProfile 메서드 업데이트**

```java
public void updateProfile(String title, String detail, String intro,
                           String professionalBackground, String coverImageUrl,
                           String proofFileUrl, Integer regularPrice,
                           Integer sessionMinutes, String timezone) {
    if (title != null) this.title = title;
    if (detail != null) this.detail = detail;
    if (intro != null) this.intro = intro;
    if (professionalBackground != null) this.professionalBackground = professionalBackground;
    if (coverImageUrl != null) this.coverImageUrl = coverImageUrl;
    if (proofFileUrl != null) this.proofFileUrl = proofFileUrl;
    if (regularPrice != null) this.regularPrice = regularPrice;
    if (sessionMinutes != null) this.sessionMinutes = sessionMinutes;
    if (timezone != null) {
        try {
            java.time.ZoneId.of(timezone); // 유효성 검증
            this.timezone = timezone;
        } catch (java.time.zone.ZoneRulesException e) {
            throw new IllegalArgumentException("유효하지 않은 timezone: " + timezone);
        }
    }
}

public void incrementReviewCount() { this.reviewCount++; }
```

**Step 5: 빌드 확인**

```bash
./gradlew compileJava
```

**Step 6: Commit**

```bash
git add -p
git commit -m "feat: CounselorProfile 엔티티에 title, detail, professionalBackground 등 필드 추가"
```

---

## Task 3: CounselorCategory 엔티티 생성 (다중 카테고리)

**Files:**
- Create: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorCategory.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorProfile.java`
- Create: `src/main/java/com/example/kbuddy_backend/livechat/repository/CounselorCategoryRepository.java`

**Step 1: CounselorCategory 엔티티 생성**

```java
// src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorCategory.java
package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.livechat.constant.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "counselor_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counselor_category",
                columnNames = {"counselor_id", "category"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private CounselorProfile counselor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Builder
    public CounselorCategory(CounselorProfile counselor, Category category) {
        this.counselor = counselor;
        this.category = category;
    }
}
```

**Step 2: CounselorProfile에 categories 컬렉션 추가**

```java
// CounselorProfile.java에 추가
@OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CounselorCategory> categories = new ArrayList<>();

public void updateCategories(List<CounselorCategory> newCategories) {
    this.categories.clear();
    this.categories.addAll(newCategories);
}
```

**Step 3: CounselorCategoryRepository 생성**

```java
package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselorCategoryRepository extends JpaRepository<CounselorCategory, Long> {
    void deleteByCounselorId(Long counselorId);
}
```

**Step 4: 빌드 확인**

```bash
./gradlew compileJava
```

**Step 5: Commit**

```bash
git add -p
git commit -m "feat: 다중 카테고리 지원을 위한 CounselorCategory 엔티티 추가"
```

---

## Task 4: CounselorPhoto 엔티티 생성

UI Step 1의 "Add photo(s)" 기능 지원.

**Files:**
- Create: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorPhoto.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorProfile.java`
- Create: `src/main/java/com/example/kbuddy_backend/livechat/repository/CounselorPhotoRepository.java`

**Step 1: CounselorPhoto 엔티티 생성**

```java
package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "counselor_photo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private CounselorProfile counselor;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    @Builder
    public CounselorPhoto(CounselorProfile counselor, String photoUrl, Integer sortOrder) {
        this.counselor = counselor;
        this.photoUrl = photoUrl;
        this.sortOrder = sortOrder;
    }
}
```

**Step 2: CounselorProfile에 photos 컬렉션 추가**

```java
@OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("sortOrder ASC")
private List<CounselorPhoto> photos = new ArrayList<>();
```

**Step 3: CounselorPhotoRepository 생성**

```java
package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CounselorPhotoRepository extends JpaRepository<CounselorPhoto, Long> {
    List<CounselorPhoto> findByCounselorIdOrderBySortOrderAsc(Long counselorId);
    void deleteByCounselorId(Long counselorId);
}
```

**Step 4: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "feat: 상담사 추가 사진 저장을 위한 CounselorPhoto 엔티티 추가"
```

---

## Task 5: CounselorPromotion 엔티티 생성

UI Step 2의 프로모션(할인가 + 세션시간 + 기간) 지원.

**Files:**
- Create: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorPromotion.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/entity/CounselorProfile.java`
- Create: `src/main/java/com/example/kbuddy_backend/livechat/repository/CounselorPromotionRepository.java`

**Step 1: CounselorPromotion 엔티티 생성**

```java
package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "counselor_promotion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorPromotion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false, unique = true)
    private CounselorProfile counselor;

    @Column(nullable = false)
    private Integer promotionalPrice;   // 할인가 (won)

    @Column(nullable = false)
    private Integer sessionMinutes;     // Amount of time (min)

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder
    public CounselorPromotion(CounselorProfile counselor, Integer promotionalPrice,
                               Integer sessionMinutes, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다");
        }
        this.counselor = counselor;
        this.promotionalPrice = promotionalPrice;
        this.sessionMinutes = sessionMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}
```

**Step 2: CounselorProfile에 promotion 연관관계 추가**

```java
@OneToOne(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private CounselorPromotion promotion;
```

**Step 3: CounselorPromotionRepository 생성**

```java
package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CounselorPromotionRepository extends JpaRepository<CounselorPromotion, Long> {
    Optional<CounselorPromotion> findByCounselorId(Long counselorId);
    void deleteByCounselorId(Long counselorId);
}
```

**Step 4: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "feat: 상담사 프로모션 저장을 위한 CounselorPromotion 엔티티 추가"
```

---

## Task 6: 응답 DTO 업데이트

`CounselorDetailResponse`, `CounselorListResponse`에 새 필드를 반영한다.

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/dto/response/CounselorDetailResponse.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/dto/response/CounselorListResponse.java`

**Step 1: CounselorDetailResponse 업데이트**

```java
package com.example.kbuddy_backend.livechat.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CounselorDetailResponse(
        String counselorId,
        String name,
        String title,
        String detail,
        String intro,
        String professionalBackground,
        String coverImageUrl,
        String proofFileUrl,
        List<String> photoUrls,
        List<String> categories,
        BigDecimal ratingAvg,
        int reviewCount,
        int regularPrice,
        int sessionMinutes,
        String timezone,
        String profileImageUrl,
        PromotionInfo promotion,
        List<RecentReview> recentReviews) {

    public record PromotionInfo(
            int promotionalPrice,
            int sessionMinutes,
            String startDate,
            String endDate,
            boolean isActive) {
    }

    public record RecentReview(
            Long reviewId,
            String customerName,
            int rating,
            String comment,
            String createdAt) {
    }
}
```

**Step 2: CounselorListResponse 업데이트**

```java
package com.example.kbuddy_backend.livechat.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CounselorListResponse(
        List<CounselorSummary> content,
        long totalElements) {

    public record CounselorSummary(
            String counselorId,
            String name,
            String title,
            List<String> categories,
            BigDecimal ratingAvg,
            int reviewCount,
            int regularPrice,
            int sessionMinutes,
            String coverImageUrl,
            boolean hasPromotion) {
    }
}
```

**Step 3: CounselorProfileService의 toSummary(), getCounselor() 수정**

변경된 필드명에 맞게 `slotRate` → `regularPrice`, 카테고리·사진 포함하도록 수정한다.

```java
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
```

**Step 4: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "feat: 응답 DTO에 title, categories, promotion 등 신규 필드 반영"
```

---

## Task 7: 프로필 등록 요청 DTO 생성

**Files:**
- Create: `src/main/java/com/example/kbuddy_backend/livechat/dto/request/RegisterCounselorRequest.java`

**Step 1: DTO 생성**

```java
package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record RegisterCounselorRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String detail,
        String intro,
        String professionalBackground,
        @NotEmpty @Size(min = 1, max = 5) List<String> categories,  // Category enum name
        @NotNull @Positive Integer regularPrice,
        @NotNull @Min(15) @Max(120) Integer sessionMinutes,
        String timezone,
        // 프로모션 (선택)
        Integer promotionalPrice,
        Integer promotionSessionMinutes,
        LocalDate promotionStartDate,
        LocalDate promotionEndDate) {
}
```

**Step 2: 프로필 수정 요청 DTO 생성**

```java
// src/main/java/com/example/kbuddy_backend/livechat/dto/request/UpdateCounselorRequest.java
package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record UpdateCounselorRequest(
        @Size(max = 100) String title,
        String detail,
        String intro,
        String professionalBackground,
        @Size(min = 1, max = 5) List<String> categories,
        @Positive Integer regularPrice,
        @Min(15) @Max(120) Integer sessionMinutes,
        String timezone,
        Integer promotionalPrice,
        Integer promotionSessionMinutes,
        LocalDate promotionStartDate,
        LocalDate promotionEndDate) {
}
```

**Step 3: Commit**

```bash
git add -p
git commit -m "feat: 프로필 등록/수정 요청 DTO 추가"
```

---

## Task 8: CounselorProfileService 등록/수정 로직 구현

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/service/CounselorProfileService.java`

**Step 1: 의존성 추가 및 registerCounselor 재구현**

```java
// 새 필드들 주입
private final CounselorCategoryRepository categoryRepository;
private final CounselorPromotionRepository promotionRepository;
private final CounselorPhotoRepository photoRepository;
private final S3Service s3Service;

@Transactional
public void registerCounselor(User user, RegisterCounselorRequest request,
                               MultipartFile coverImage, MultipartFile proofFile,
                               List<MultipartFile> photos) {
    if (counselorProfileRepository.existsByUserId(user.getId())) {
        throw new IllegalStateException("이미 상담사로 등록된 사용자입니다");
    }

    String coverImageUrl = uploadIfPresent(coverImage, "counselor/cover");
    String proofFileUrl = uploadIfPresent(proofFile, "counselor/proof");

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

    // 카테고리 저장
    saveCategories(profile, request.categories());

    // 추가 사진 저장
    if (photos != null && !photos.isEmpty()) {
        savePhotos(profile, photos);
    }

    // 프로모션 저장
    if (request.promotionalPrice() != null) {
        savePromotion(profile, request);
    }
}

@Transactional
public void updateCounselor(User user, UpdateCounselorRequest request,
                              MultipartFile coverImage, MultipartFile proofFile,
                              List<MultipartFile> photos) {
    CounselorProfile profile = counselorProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("상담사로 등록되지 않은 사용자입니다"));

    String coverImageUrl = coverImage != null ? uploadIfPresent(coverImage, "counselor/cover") : null;
    String proofFileUrl = proofFile != null ? uploadIfPresent(proofFile, "counselor/proof") : null;

    profile.updateProfile(request.title(), request.detail(), request.intro(),
            request.professionalBackground(), coverImageUrl, proofFileUrl,
            request.regularPrice(), request.sessionMinutes(), request.timezone());

    if (request.categories() != null) {
        categoryRepository.deleteByCounselorId(profile.getId());
        saveCategories(profile, request.categories());
    }

    if (photos != null && !photos.isEmpty()) {
        photoRepository.deleteByCounselorId(profile.getId());
        savePhotos(profile, photos);
    }

    if (request.promotionalPrice() != null) {
        promotionRepository.deleteByCounselorId(profile.getId());
        savePromotion(profile, request);
    }
}

private String uploadIfPresent(MultipartFile file, String folder) {
    if (file == null || file.isEmpty()) return null;
    if (!s3Service.checkImageFile(file)) {
        throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
    }
    return s3Service.saveFileWithUUID(file, folder).getUrl();
}

private void saveCategories(CounselorProfile profile, List<String> categoryNames) {
    categoryNames.stream()
            .map(name -> {
                try { return Category.valueOf(name); }
                catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("유효하지 않은 카테고리: " + name);
                }
            })
            .map(cat -> CounselorCategory.builder().counselor(profile).category(cat).build())
            .forEach(categoryRepository::save);
}

private void savePhotos(CounselorProfile profile, List<MultipartFile> photos) {
    for (int i = 0; i < photos.size(); i++) {
        String url = s3Service.saveFileWithUUID(photos.get(i), "counselor/photos").getUrl();
        photoRepository.save(CounselorPhoto.builder()
                .counselor(profile).photoUrl(url).sortOrder(i).build());
    }
}
```

**Step 2: 빌드 확인**

```bash
./gradlew compileJava
```

**Step 3: Commit**

```bash
git add -p
git commit -m "feat: 상담사 프로필 등록/수정 서비스 로직 구현"
```

---

## Task 9: 프로필 등록/수정 컨트롤러 추가

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/controller/CounselorController.java`

**Step 1: POST, PATCH 엔드포인트 추가**

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "상담사 프로필 등록", description = "상담사 프로필을 등록합니다. 커버 이미지, 자격증 파일, 추가 사진을 multipart로 함께 전송합니다.")
public ResponseEntity<Void> registerCounselor(
        @Valid @RequestPart("data") RegisterCounselorRequest request,
        @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
        @RequestPart(value = "proofFile", required = false) MultipartFile proofFile,
        @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
        @Parameter(hidden = true) @CurrentUser User user) {
    counselorProfileService.registerCounselor(user, request, coverImage, proofFile, photos);
    return ResponseEntity.status(HttpStatus.CREATED).build();
}

@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "상담사 프로필 수정", description = "상담사 프로필을 수정합니다. 변경할 필드만 포함합니다.")
public ResponseEntity<Void> updateCounselor(
        @Valid @RequestPart("data") UpdateCounselorRequest request,
        @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
        @RequestPart(value = "proofFile", required = false) MultipartFile proofFile,
        @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
        @Parameter(hidden = true) @CurrentUser User user) {
    counselorProfileService.updateCounselor(user, request, coverImage, proofFile, photos);
    return ResponseEntity.noContent().build();
}
```

**Step 2: 빌드 확인**

```bash
./gradlew compileJava
```

**Step 3: Commit**

```bash
git add -p
git commit -m "feat: 상담사 프로필 등록(POST) / 수정(PATCH) 컨트롤러 추가"
```

---

## Task 10: 가용시간 날짜 범위 일괄 등록 + 삭제 API

UI Step 2의 "03/08 - 03/14 | 6PM - 9PM" 날짜 범위 입력 지원 및 삭제 API 추가.

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/dto/request/CreateAvailabilityBulkRequest.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/service/CounselorAvailabilityService.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/controller/CounselorAvailabilityController.java`

**Step 1: CreateAvailabilityBulkRequest를 날짜 범위로 확장**

```java
package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAvailabilityBulkRequest(
        @NotNull LocalDate startDate,  // date → startDate
        @NotNull LocalDate endDate,    // 추가
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime) {
}
```

**Step 2: CounselorAvailabilityService에 날짜 범위 처리 추가**

```java
@Transactional
public List<CounselorAvailability> addAvailabilityBulk(User counselor,
        LocalDate startDate, LocalDate endDate,
        LocalTime startTime, LocalTime endTime) {
    CounselorProfile profile = getCounselorProfile(counselor);
    List<CounselorAvailability> created = new ArrayList<>();

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
        LocalTime time = startTime;
        while (time.isBefore(endTime)) {
            if (!availabilityRepository.existsByCounselorAndSlotDateAndSlotStartTime(profile, current, time)) {
                created.add(availabilityRepository.save(
                        CounselorAvailability.builder()
                                .counselor(profile)
                                .slotDate(current)
                                .slotStartTime(time)
                                .build()));
            }
            time = time.plusMinutes(30);
        }
        current = current.plusDays(1);
    }
    return created;
}
```

**Step 3: 컨트롤러 bulk 엔드포인트 수정 + DELETE 추가**

```java
// bulk 수정
@PostMapping("/bulk")
public ResponseEntity<CounselorAvailabilityResponse> createAvailabilityBulk(...) {
    List<CounselorAvailability> availabilities = availabilityService.addAvailabilityBulk(
            user, request.startDate(), request.endDate(),
            request.startTime(), request.endTime());
    ...
}

// DELETE 추가
@DeleteMapping("/{availabilityId}")
@Operation(summary = "상담 가능 시간 삭제", description = "상담 가능 시간 슬롯을 삭제합니다. AVAILABLE 상태만 삭제 가능합니다.")
public ResponseEntity<Void> deleteAvailability(
        @PathVariable Long availabilityId,
        @Parameter(hidden = true) @CurrentUser User user) {
    availabilityService.removeAvailability(user, availabilityId);
    return ResponseEntity.noContent().build();
}
```

**Step 4: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "feat: 가용시간 날짜범위 일괄 등록 및 삭제 API 추가"
```

---

## Task 11: BookingReserveRequest 수정

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/dto/request/BookingReserveRequest.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/entity/Booking.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/service/BookingService.java`

**Step 1: DTO 수정**

```java
package com.example.kbuddy_backend.livechat.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record BookingReserveRequest(
        @NotEmpty @Size(min = 1, max = 8) List<Long> slotIds,
        @NotBlank @Size(max = 200) String topic,
        @Size(max = 500) String memo) {
}
```

`counselorId`는 서비스에서 슬롯 소유자로 검증하므로 제거한다.

**Step 2: Booking 엔티티에 topic, memo 추가**

```java
// Booking.java에 필드 추가
@Column(length = 200)
private String topic;

@Column(columnDefinition = "TEXT")
private String memo;

// Builder 파라미터에 추가
.topic(topic)
.memo(memo)
```

**Step 3: BookingService.reserve() 수정**

- `counselorId` 검증 제거
- `topic`, `memo` 저장 추가

```java
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
```

**Step 4: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "fix: BookingReserveRequest에 topic/memo 추가, 데드 필드 counselorId 제거"
```

---

## Task 12: 예약 관리 API (결제확인 / 완료 / 취소 / 목록)

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/controller/BookingController.java`
- Create: `src/main/java/com/example/kbuddy_backend/livechat/dto/response/BookingListResponse.java`
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/service/BookingService.java`

**Step 1: BookingListResponse DTO 생성**

```java
package com.example.kbuddy_backend.livechat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BookingListResponse(
        List<BookingSummary> content,
        long totalElements) {

    public record BookingSummary(
            Long bookingId,
            String counselorName,
            String counselorCoverImageUrl,
            String topic,
            String status,
            int totalPrice,
            LocalDateTime bookingStartUtc,
            LocalDateTime bookingEndUtc) {
    }
}
```

**Step 2: BookingService에 목록 조회 추가**

```java
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
```

**Step 3: BookingController에 엔드포인트 추가**

```java
@PatchMapping("/{bookingId}/confirm")
@Operation(summary = "결제 확인", description = "예약을 결제 완료 상태로 전환합니다.")
public ResponseEntity<Void> confirmPayment(@PathVariable Long bookingId) {
    bookingService.confirmPayment(bookingId);
    return ResponseEntity.noContent().build();
}

@PatchMapping("/{bookingId}/complete")
@Operation(summary = "상담 완료", description = "상담 완료 처리합니다.")
public ResponseEntity<Void> completeBooking(@PathVariable Long bookingId) {
    bookingService.completeBooking(bookingId);
    return ResponseEntity.noContent().build();
}

@DeleteMapping("/{bookingId}")
@Operation(summary = "예약 취소", description = "예약을 취소합니다.")
public ResponseEntity<Void> cancelBooking(
        @PathVariable Long bookingId,
        @Parameter(hidden = true) @CurrentUser User user) {
    bookingService.cancelBooking(user, bookingId);
    return ResponseEntity.noContent().build();
}

@GetMapping("/my")
@Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예약 목록을 조회합니다.")
public ResponseEntity<BookingListResponse> getMyBookings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @Parameter(hidden = true) @CurrentUser User user) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(bookingService.getMyBookings(user, pageable));
}
```

**Step 4: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "feat: 예약 결제확인/완료/취소/목록 API 추가"
```

---

## Task 13: reviewCount 카운터 연동

`CounselorReviewService`에서 리뷰 생성 시 `reviewCount` 증가.

**Files:**
- Modify: `src/main/java/com/example/kbuddy_backend/livechat/service/CounselorReviewService.java`

**Step 1: createReview에 reviewCount 증가 추가**

```java
reviewRepository.save(review);
profile.incrementReviewCount();   // 추가
updateCounselorRating(booking.getCounselor().getId());
```

**Step 2: getCounselor(), toSummary()에서 DB COUNT 쿼리 제거**

`CounselorProfileService`에서 `reviewRepository.countByCounselorId()` 호출을 제거하고 `profile.getReviewCount()` 사용.

**Step 3: 빌드 확인 및 Commit**

```bash
./gradlew compileJava
git add -p
git commit -m "fix: N+1 제거 - reviewCount를 CounselorProfile에 캐싱"
```

---

## 완성 후 전체 API 목록

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/kbuddy/v1/counselor` | 상담사 목록 조회 |
| GET | `/kbuddy/v1/counselor/{id}` | 상담사 상세 조회 |
| POST | `/kbuddy/v1/counselor` | 상담사 프로필 등록 (**신규**) |
| PATCH | `/kbuddy/v1/counselor` | 상담사 프로필 수정 (**신규**) |
| GET | `/kbuddy/v1/counselor/{id}/availability` | 가용시간 조회 |
| POST | `/kbuddy/v1/counselor/availability` | 가용시간 단일 추가 |
| POST | `/kbuddy/v1/counselor/availability/bulk` | 가용시간 날짜범위 일괄 추가 (**수정**) |
| DELETE | `/kbuddy/v1/counselor/availability/{id}` | 가용시간 삭제 (**신규**) |
| POST | `/kbuddy/v1/booking/reserve` | 예약 선점 |
| GET | `/kbuddy/v1/booking/my` | 내 예약 목록 (**신규**) |
| PATCH | `/kbuddy/v1/booking/{id}/confirm` | 결제 확인 (**신규**) |
| PATCH | `/kbuddy/v1/booking/{id}/complete` | 상담 완료 (**신규**) |
| DELETE | `/kbuddy/v1/booking/{id}` | 예약 취소 (**신규**) |
| POST | `/kbuddy/v1/review` | 리뷰 작성 |
| POST | `/kbuddy/v1/counselor/{id}/inquiry` | 문의글 작성 |
