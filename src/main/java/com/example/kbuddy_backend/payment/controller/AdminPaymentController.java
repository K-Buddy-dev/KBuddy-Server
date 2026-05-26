package com.example.kbuddy_backend.payment.controller;

import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import com.example.kbuddy_backend.payment.constant.PaymentStatus;
import com.example.kbuddy_backend.payment.dto.request.PaymentCancelRequest;
import com.example.kbuddy_backend.payment.dto.request.PaymentConfirmRequest;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;
import com.example.kbuddy_backend.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/admin/payments")
@Tag(name = "Admin Payment API", description = "관리자 결제 API")
public class AdminPaymentController {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "관리자 결제 목록 조회", description = "결제 상태로 필터링해 결제 목록을 조회합니다.")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        validateAdmin();
        return ResponseEntity.ok(paymentService.getPayments(status, pageable));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "관리자 결제 상세 조회", description = "결제 상세 정보를 조회합니다.")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        validateAdmin();
        return ResponseEntity.ok(paymentService.getPaymentForAdmin(paymentId));
    }

    @PatchMapping("/{paymentId}/confirm-deposit")
    @Operation(summary = "입금 확인", description = "관리자가 무통장 입금을 확인하고 예약을 결제 완료 상태로 전환합니다.")
    public ResponseEntity<PaymentResponse> confirmDeposit(
            @PathVariable Long paymentId,
            @RequestBody(required = false) PaymentConfirmRequest request) {
        validateAdmin();
        return ResponseEntity.ok(paymentService.confirmDeposit(paymentId, request));
    }

    @PatchMapping("/{paymentId}/cancel")
    @Operation(summary = "결제 취소", description = "관리자가 입금 대기 결제를 취소합니다.")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request) {
        validateAdmin();
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId, request));
    }

    private void validateAdmin() {
        boolean hasAdminRole = SecurityContextHolder.getContextHolderStrategy().getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> ADMIN_ROLE.equals(authority.getAuthority()));

        if (!hasAdminRole) {
            throw new UnauthorizedException("관리자 권한이 없습니다");
        }
    }
}
