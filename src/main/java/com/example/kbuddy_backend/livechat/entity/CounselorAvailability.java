package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.livechat.constant.SlotStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "counselor_availability",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counselor_slot",
                columnNames = {"counselor_id", "slot_start_utc"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorAvailability extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private CounselorProfile counselor;

    @Column(name = "slot_start_utc", nullable = false)
    private LocalDateTime slotStartUtc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status;

    @Version
    private Integer version;

    @Builder
    public CounselorAvailability(CounselorProfile counselor, LocalDateTime slotStartUtc) {
        validateSlotTime(slotStartUtc);
        this.counselor = counselor;
        this.slotStartUtc = slotStartUtc;
        this.status = SlotStatus.AVAILABLE;
    }

    public void book() {
        if (this.status != SlotStatus.AVAILABLE) {
            throw new IllegalStateException("예약 가능한 상태의 슬롯만 예약할 수 있습니다");
        }
        this.status = SlotStatus.BOOKED;
    }

    public void cancelBooking() {
        if (this.status != SlotStatus.BOOKED) {
            throw new IllegalStateException("예약된 슬롯만 취소할 수 있습니다");
        }
        this.status = SlotStatus.AVAILABLE;
    }

    public void block() {
        if (this.status != SlotStatus.AVAILABLE) {
            throw new IllegalStateException("예약 가능한 상태의 슬롯만 차단할 수 있습니다");
        }
        this.status = SlotStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != SlotStatus.BLOCKED) {
            throw new IllegalStateException("차단된 슬롯만 해제할 수 있습니다");
        }
        this.status = SlotStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == SlotStatus.AVAILABLE;
    }

    private void validateSlotTime(LocalDateTime utc) {
        if (utc.getMinute() % 30 != 0 || utc.getSecond() != 0) {
            throw new IllegalArgumentException("슬롯 시간은 30분 단위여야 합니다 (예: 09:00, 09:30)");
        }
    }
}
