package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @Column(nullable = false)
    private LocalDateTime bookingStartUtc;

    @Column(nullable = false)
    private LocalDateTime bookingEndUtc;

    @Column(nullable = false)
    private Integer slotCount;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSlot> bookingSlots = new ArrayList<>();

    @Builder
    public Booking(User customer, User counselor, LocalDateTime bookingStartUtc,
                   LocalDateTime bookingEndUtc, Integer slotCount, Integer totalPrice) {
        this.customer = customer;
        this.counselor = counselor;
        this.bookingStartUtc = bookingStartUtc;
        this.bookingEndUtc = bookingEndUtc;
        this.slotCount = slotCount;
        this.totalPrice = totalPrice;
        this.status = BookingStatus.PENDING;
    }

    public void addBookingSlot(BookingSlot bookingSlot) {
        this.bookingSlots.add(bookingSlot);
    }

    public void confirmPayment() {
        if (this.status != BookingStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태의 예약만 결제 확인이 가능합니다");
        }
        this.status = BookingStatus.PAID;
    }

    public void complete() {
        if (this.status != BookingStatus.PAID) {
            throw new IllegalStateException("결제 완료된 예약만 완료 처리가 가능합니다");
        }
        this.status = BookingStatus.COMPLETED;
    }

    public void cancel(String reason) {
        if (this.status == BookingStatus.COMPLETED || this.status == BookingStatus.REFUNDED) {
            throw new IllegalStateException("완료되었거나 환불된 예약은 취소할 수 없습니다");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelReason = reason;
        // 관련 슬롯들을 AVAILABLE로 복원
        for (BookingSlot slot : this.bookingSlots) {
            slot.getAvailability().cancelBooking();
        }
    }

    public void cancel() {
        cancel(null);
    }

    public void refund() {
        if (this.status != BookingStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약만 환불 처리가 가능합니다");
        }
        this.status = BookingStatus.REFUNDED;
    }
}
