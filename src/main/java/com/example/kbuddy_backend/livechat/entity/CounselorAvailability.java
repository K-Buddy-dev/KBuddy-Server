package com.example.kbuddy_backend.livechat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "counselor_availability")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private CounselorProfile counselor;

    @Column(nullable = false)
    private LocalDate availableDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private boolean isBooked = false;

    @Version
    private Integer version;

    @Builder
    public CounselorAvailability(CounselorProfile counselor, LocalDate availableDate, LocalTime startTime) {
        this.counselor = counselor;
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.isBooked = false;
    }

    public void book() {
        if (this.isBooked) {
            throw new IllegalStateException("이미 예약된 슬롯입니다");
        }
        this.isBooked = true;
    }

    public void cancelBooking() {
        this.isBooked = false;
    }
}
