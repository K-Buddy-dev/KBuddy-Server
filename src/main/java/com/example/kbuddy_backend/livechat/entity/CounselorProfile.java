package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "counselor_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("del_yn = false")
public class CounselorProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private final UUID uuid = UUID.randomUUID();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String intro;

    @Column(columnDefinition = "TEXT")
    private String professionalBackground;

    private String coverImageUrl;

    private String proofFileUrl;

    @Column(nullable = false)
    private Integer regularPrice;

    @Column(nullable = false)
    private Integer sessionMinutes;

    @Column(length = 50)
    private String timezone;

    @Column(precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Column(nullable = false)
    private Integer reviewCount;

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CounselorCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CounselorPhoto> photos = new ArrayList<>();

    @OneToOne(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CounselorPromotion promotion;

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
                java.time.ZoneId.of(timezone);
                this.timezone = timezone;
            } catch (java.time.zone.ZoneRulesException e) {
                throw new IllegalArgumentException("유효하지 않은 timezone: " + timezone);
            }
        }
    }

    public void incrementReviewCount() {
        this.reviewCount++;
    }

    public void updateRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public void updateCategories(List<CounselorCategory> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
    }
}
