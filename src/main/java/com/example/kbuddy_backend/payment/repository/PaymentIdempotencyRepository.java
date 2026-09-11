package com.example.kbuddy_backend.payment.repository;

import com.example.kbuddy_backend.payment.entity.PaymentIdempotency;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotency, Long> {

    // 요청의 멱등키로 최초 처리 상태와 저장된 응답을 조회한다.
    Optional<PaymentIdempotency> findByIdempotencyKey(String idempotencyKey);
}
