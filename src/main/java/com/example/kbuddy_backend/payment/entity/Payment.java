package com.example.kbuddy_backend.payment.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.payment.constant.PaymentMethod;
import com.example.kbuddy_backend.payment.constant.PaymentStatus;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_booking",
                columnNames = "booking_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    // 애플리케이션 조회가 동시에 통과해도 DB 유니크 제약조건이 예약당 결제를 한 건으로 제한한다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal platformFeeRate;

    @Column(nullable = false)
    private Integer platformFeeAmount;

    @Column(nullable = false)
    private Integer counselorSettlementAmount;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountHolder;

    @Column(nullable = false)
    private LocalDateTime depositDueAt;

    private String depositorName;

    private LocalDateTime depositReportedAt;

    private Integer confirmedAmount;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Column(columnDefinition = "TEXT")
    private String customerMemo;

    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    @Builder
    public Payment(Booking booking, User customer, User counselor, Integer totalAmount,
                   BigDecimal platformFeeRate, String bankName, String accountNumber,
                   String accountHolder, LocalDateTime depositDueAt, String depositorName) {
        this.booking = booking;
        this.customer = customer;
        this.counselor = counselor;
        this.method = PaymentMethod.BANK_TRANSFER;
        this.status = PaymentStatus.AWAITING_DEPOSIT;
        this.totalAmount = totalAmount;
        this.platformFeeRate = platformFeeRate;
        this.platformFeeAmount = calculatePlatformFee(totalAmount, platformFeeRate);
        this.counselorSettlementAmount = totalAmount - this.platformFeeAmount;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.depositDueAt = depositDueAt;
        this.depositorName = depositorName;
    }

    public void reportDeposit(String depositorName, String customerMemo) {
        if (this.status != PaymentStatus.AWAITING_DEPOSIT && this.status != PaymentStatus.DEPOSIT_REPORTED) {
            throw new IllegalArgumentException("입금 대기 상태의 결제만 입금 완료 신고가 가능합니다");
        }
        this.status = PaymentStatus.DEPOSIT_REPORTED;
        this.depositorName = depositorName;
        this.customerMemo = customerMemo;
        this.depositReportedAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    public void confirmDeposit(Integer confirmedAmount, String adminMemo) {
        if (this.status != PaymentStatus.AWAITING_DEPOSIT && this.status != PaymentStatus.DEPOSIT_REPORTED) {
            throw new IllegalArgumentException("입금 대기 또는 입금 완료 신고 상태의 결제만 확인할 수 있습니다");
        }
        int amountToConfirm = confirmedAmount == null ? this.totalAmount : confirmedAmount;
        if (amountToConfirm != this.totalAmount) {
            throw new IllegalArgumentException("입금 확인 금액이 결제 금액과 일치하지 않습니다");
        }
        this.status = PaymentStatus.PAID;
        this.confirmedAmount = amountToConfirm;
        this.adminMemo = adminMemo;
        this.paidAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    public void cancel(String reason) {
        if (this.status == PaymentStatus.PAID || this.status == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("입금 확인 또는 환불 완료된 결제는 취소할 수 없습니다");
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    public void expire() {
        if (this.status != PaymentStatus.AWAITING_DEPOSIT) {
            throw new IllegalArgumentException("입금 대기 상태의 결제만 만료 처리할 수 있습니다");
        }
        this.status = PaymentStatus.EXPIRED;
        this.cancelledAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    private int calculatePlatformFee(Integer totalAmount, BigDecimal platformFeeRate) {
        return BigDecimal.valueOf(totalAmount)
                .multiply(platformFeeRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
