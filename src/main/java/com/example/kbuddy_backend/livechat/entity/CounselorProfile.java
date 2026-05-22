package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "counselor_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String intro;

    @Column(nullable = false)
    private Integer slotRate;

    @Column(length = 50)
    private String timezone;

    @Column(precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Builder
    public CounselorProfile(User user, String intro, Integer slotRate, String timezone) {
        this.user = user;
        this.intro = intro;
        this.slotRate = slotRate;
        this.timezone = timezone != null ? timezone : "UTC";
        this.ratingAvg = BigDecimal.ZERO;
    }

    public void updateProfile(String intro, Integer slotRate, String timezone) {
        if (intro != null) {
            this.intro = intro;
        }
        if (slotRate != null) {
            this.slotRate = slotRate;
        }
        if (timezone != null) {
            this.timezone = timezone;
        }
    }

    public void updateRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg;
    }
}
