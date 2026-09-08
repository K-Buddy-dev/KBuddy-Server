package com.example.kbuddy_backend.payment.service;

import com.example.kbuddy_backend.common.exception.IdempotencyConflictException;
import com.example.kbuddy_backend.payment.constant.PaymentIdempotencyOperation;
import com.example.kbuddy_backend.payment.constant.PaymentIdempotencyStatus;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;
import com.example.kbuddy_backend.payment.entity.Payment;
import com.example.kbuddy_backend.payment.entity.PaymentIdempotency;
import com.example.kbuddy_backend.payment.repository.PaymentIdempotencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentIdempotencyService {

    private final PaymentIdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    /** 결제 상태 변경과 멱등 기록을 같은 트랜잭션으로 묶어 둘 중 하나만 저장되는 상태를 막는다. */
    @Transactional(propagation = Propagation.MANDATORY)
    public StartResult start(
            String idempotencyKey,
            PaymentIdempotencyOperation operation,
            Payment payment,
            String requestHash) {
        Optional<PaymentIdempotency> existing =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            // 기존 키가 있으면 요청 내용과 처리 상태를 검증해 재사용 또는 충돌 여부를 결정한다.
            return handleExisting(existing.get(), operation, payment, requestHash);
        }

        PaymentIdempotency idempotency =
                PaymentIdempotency.start(idempotencyKey, operation, payment, requestHash);
        try {
            // flush로 충돌을 즉시 확인해 같은 키의 동시 요청이 업무 로직에 진입하지 못하게 한다.
            idempotencyRepository.saveAndFlush(idempotency);
        } catch (DataIntegrityViolationException exception) {
            throw new IdempotencyConflictException(
                    "동일한 멱등키 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }
        return StartResult.started(idempotency);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(PaymentIdempotency idempotency, PaymentResponse response) {
        try {
            // 최초 성공 응답을 저장해 이후 재시도에도 완전히 같은 응답을 반환한다.
            idempotency.complete(200, objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("결제 멱등 응답을 저장할 수 없습니다", exception);
        }
    }

    public String createConfirmRequestHash(Long paymentId, Integer confirmedAmount, String adminMemo) {
        // 멱등키가 같아도 실제 요청 값이 다른 경우를 구분할 수 있도록 비교용 해시를 만든다.
        String normalized = paymentId + "\n" + confirmedAmount + "\n" + normalizeMemo(adminMemo);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다", exception);
        }
    }

    private StartResult handleExisting(
            PaymentIdempotency existing,
            PaymentIdempotencyOperation operation,
            Payment payment,
            String requestHash) {
        if (existing.getOperation() != operation
                || !existing.getPayment().getId().equals(payment.getId())
                || !existing.getRequestHash().equals(requestHash)) {
            // 하나의 키를 다른 결제나 요청 값에 재사용하면 최초 요청의 의미가 깨지므로 거부한다.
            throw new IdempotencyConflictException(
                    "동일한 멱등키가 다른 결제 요청에 사용되었습니다.");
        }

        if (existing.getStatus() == PaymentIdempotencyStatus.PROCESSING) {
            // 최초 요청이 아직 끝나지 않았으므로 중복 실행하지 않고 잠시 뒤 재시도하도록 알린다.
            throw new IdempotencyConflictException(
                    "동일한 멱등키 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 완료된 요청은 저장한 최초 응답을 복원해 업무 로직 실행 없이 반환한다.
            return StartResult.completed(
                    existing,
                    objectMapper.readValue(existing.getResponseBody(), PaymentResponse.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 결제 멱등 응답을 읽을 수 없습니다", exception);
        }
    }

    private String normalizeMemo(String adminMemo) {
        return adminMemo == null ? "" : adminMemo;
    }

    public record StartResult(
            PaymentIdempotency idempotency,
            PaymentResponse completedResponse) {

        static StartResult started(PaymentIdempotency idempotency) {
            return new StartResult(idempotency, null);
        }

        static StartResult completed(
                PaymentIdempotency idempotency,
                PaymentResponse completedResponse) {
            return new StartResult(idempotency, completedResponse);
        }

        public boolean isCompleted() {
            return completedResponse != null;
        }
    }
}
