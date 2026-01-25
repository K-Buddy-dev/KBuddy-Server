package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.livechat.constant.Specialty;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Specialty specialty;

    @Column(nullable = false)
    private Integer hourlyRate;

    @Column(precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Builder
    public CounselorProfile(User user, String intro, Specialty specialty, Integer hourlyRate) {
        this.user = user;
        this.intro = intro;
        this.specialty = specialty;
        this.hourlyRate = hourlyRate;
        this.ratingAvg = BigDecimal.ZERO;
    }

    public void updateProfile(String intro, Specialty specialty, Integer hourlyRate) {
        if (intro != null) {
            this.intro = intro;
        }
        if (specialty != null) {
            this.specialty = specialty;
        }
        if (hourlyRate != null) {
            this.hourlyRate = hourlyRate;
        }
    }

    public void updateRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg;
    }
}
