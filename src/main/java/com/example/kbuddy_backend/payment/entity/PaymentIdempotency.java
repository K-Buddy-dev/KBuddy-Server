package com.example.kbuddy_backend.payment.entity;

import com.example.kbuddy_backend.payment.constant.PaymentIdempotencyOperation;
import com.example.kbuddy_backend.payment.constant.PaymentIdempotencyStatus;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "payment_idempotency",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_idempotency_key",
                columnNames = "idempotency_key"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/** 결제 승인 요청의 처리 상태와 최초 성공 응답을 저장해 동일 요청의 중복 실행을 방지한다. */
public class PaymentIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 36)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentIdempotencyOperation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentIdempotencyStatus status;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static PaymentIdempotency start(
            String idempotencyKey,
            PaymentIdempotencyOperation operation,
            Payment payment,
            String requestHash) {
        // 요청을 선점한 시점에는 PROCESSING으로 저장해 같은 키의 동시 요청 실행을 막는다.
        PaymentIdempotency idempotency = new PaymentIdempotency();
        idempotency.idempotencyKey = idempotencyKey;
        idempotency.operation = operation;
        idempotency.payment = payment;
        idempotency.requestHash = requestHash;
        idempotency.status = PaymentIdempotencyStatus.PROCESSING;
        return idempotency;
    }

    public void complete(int responseStatus, String responseBody) {
        // 승인과 후속 작업이 끝난 뒤 최초 응답과 완료 시각을 함께 기록한다.
        this.status = PaymentIdempotencyStatus.COMPLETED;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.completedAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}
