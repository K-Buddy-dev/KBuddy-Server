package com.example.kbuddy_backend.payment.service;

import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.NotFoundException;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.payment.config.BankTransferPaymentProperties;
import com.example.kbuddy_backend.payment.entity.Payment;
import com.example.kbuddy_backend.payment.repository.PaymentRepository;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCreationService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BankTransferPaymentProperties paymentProperties;

    /**
     * 결제 생성 실패 트랜잭션을 바깥 요청과 분리한다.
     * 동시 요청이 유니크 제약조건에 걸리면 먼저 롤백한 뒤 기존 결제를 안전하게 다시 조회할 수 있다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse createForCommittedBooking(Long bookingId, String depositorName) {
        // 트랜잭션 시작 후 다시 조회해 앞선 동시 요청이 이미 만든 결제가 있으면 그대로 반환한다.
        return paymentRepository.findByBookingId(bookingId)
                .map(PaymentResponse::from)
                .orElseGet(() -> PaymentResponse.from(createNewPayment(bookingId, depositorName)));
    }

    private Payment createNewPayment(Long bookingId, String depositorName) {
        // 바깥 트랜잭션의 엔티티 대신 현재 트랜잭션에서 확정된 예약을 다시 조회한다.
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("결제 대기 상태의 예약만 결제를 생성할 수 있습니다");
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .customer(booking.getCustomer())
                .counselor(booking.getCounselor())
                .totalAmount(booking.getTotalPrice())
                .platformFeeRate(paymentProperties.getPlatformFeeRate())
                .bankName(paymentProperties.getBankName())
                .accountNumber(paymentProperties.getAccountNumber())
                .accountHolder(paymentProperties.getAccountHolder())
                .depositDueAt(LocalDateTime.now(java.time.ZoneOffset.UTC)
                        .plusHours(paymentProperties.getDepositTimeoutHours()))
                .depositorName(depositorName)
                .build();
        // 즉시 flush해 유니크 충돌을 여기서 감지하고 호출자가 기존 결제로 복구하게 한다.
        return paymentRepository.saveAndFlush(payment);
    }
}
