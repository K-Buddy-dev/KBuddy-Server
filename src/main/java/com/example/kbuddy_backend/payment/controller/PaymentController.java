package com.example.kbuddy_backend.payment.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.payment.dto.request.BankTransferPaymentCreateRequest;
import com.example.kbuddy_backend.payment.dto.request.DepositReportRequest;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;
import com.example.kbuddy_backend.payment.service.PaymentService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/payments")
@Tag(name = "Payment API", description = "결제 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/bank-transfer")
    @Operation(summary = "무통장 입금 결제 생성", description = "예약 ID로 무통장 입금 결제를 생성하고 입금 안내 정보를 반환합니다.")
    public ResponseEntity<PaymentResponse> createBankTransferPayment(
            @Valid @RequestBody BankTransferPaymentCreateRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(paymentService.createBankTransferPayment(user, request));
    }

    @GetMapping
    @Operation(summary = "내 결제 목록 조회", description = "로그인한 사용자의 결제 목록을 조회합니다.")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(paymentService.getMyPayments(user, pageable));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 상세 조회", description = "로그인한 사용자의 결제 상세 정보를 조회합니다.")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId,
            @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(paymentService.getPayment(user, paymentId));
    }

    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "예약 결제 조회", description = "예약 ID로 결제 정보를 조회합니다.")
    public ResponseEntity<PaymentResponse> getPaymentByBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(user, bookingId));
    }

    @PatchMapping("/{paymentId}/deposit-report")
    @Operation(summary = "입금 완료 신고", description = "내담자가 입금자명과 메모를 제출해 입금 완료를 알립니다.")
    public ResponseEntity<PaymentResponse> reportDeposit(
            @PathVariable Long paymentId,
            @Valid @RequestBody DepositReportRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(paymentService.reportDeposit(user, paymentId, request));
    }
}
