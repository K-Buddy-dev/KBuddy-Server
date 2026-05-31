package com.example.kbuddy_backend.payment.dto.response;

import com.example.kbuddy_backend.payment.constant.PaymentMethod;
import com.example.kbuddy_backend.payment.constant.PaymentStatus;
import com.example.kbuddy_backend.payment.entity.Payment;
import com.example.kbuddy_backend.user.util.UserNameUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long bookingId,
        Long customerId,
        String customerName,
        String customerUsername,
        Long counselorId,
        String counselorName,
        String counselorUsername,
        PaymentMethod method,
        PaymentStatus status,
        Integer totalAmount,
        BigDecimal platformFeeRate,
        Integer platformFeeAmount,
        Integer counselorSettlementAmount,
        String bankName,
        String accountNumber,
        String accountHolder,
        String depositorName,
        LocalDateTime depositDueAt,
        LocalDateTime depositReportedAt,
        Integer confirmedAmount,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        String cancelReason,
        String customerMemo,
        String adminMemo,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getCustomer().getId(),
                UserNameUtils.fullName(payment.getCustomer()),
                payment.getCustomer().getUsername(),
                payment.getCounselor().getId(),
                UserNameUtils.fullName(payment.getCounselor()),
                payment.getCounselor().getUsername(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTotalAmount(),
                payment.getPlatformFeeRate(),
                payment.getPlatformFeeAmount(),
                payment.getCounselorSettlementAmount(),
                payment.getBankName(),
                payment.getAccountNumber(),
                payment.getAccountHolder(),
                payment.getDepositorName(),
                payment.getDepositDueAt(),
                payment.getDepositReportedAt(),
                payment.getConfirmedAmount(),
                payment.getPaidAt(),
                payment.getCancelledAt(),
                payment.getCancelReason(),
                payment.getCustomerMemo(),
                payment.getAdminMemo(),
                payment.getCreatedDate(),
                payment.getLastModifiedDate()
        );
    }
}
