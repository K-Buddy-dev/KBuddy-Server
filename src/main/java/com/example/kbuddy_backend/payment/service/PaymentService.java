package com.example.kbuddy_backend.payment.service;

import com.example.kbuddy_backend.chat.service.ChatRoomService;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.NotFoundException;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.payment.config.BankTransferPaymentProperties;
import com.example.kbuddy_backend.payment.constant.PaymentStatus;
import com.example.kbuddy_backend.payment.dto.request.BankTransferPaymentCreateRequest;
import com.example.kbuddy_backend.payment.dto.request.DepositReportRequest;
import com.example.kbuddy_backend.payment.dto.request.PaymentCancelRequest;
import com.example.kbuddy_backend.payment.dto.request.PaymentConfirmRequest;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;
import com.example.kbuddy_backend.payment.entity.Payment;
import com.example.kbuddy_backend.payment.repository.PaymentRepository;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.service.NotificationService;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.util.UserNameUtils;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BankTransferPaymentProperties bankTransferPaymentProperties;
    private final ChatRoomService chatRoomService;
    private final CounselorProfileRepository counselorProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse createBankTransferPayment(User customer, BankTransferPaymentCreateRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다"));

        validateBookingCustomer(customer, booking);

        return paymentRepository.findByBookingId(booking.getId())
                .map(PaymentResponse::from)
                .orElseGet(() -> createPayment(booking, request.depositorName()));
    }

    public PaymentResponse getPayment(User customer, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndCustomer(paymentId, customer)
                .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다"));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByBooking(User customer, Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다"));
        validatePaymentCustomer(customer, payment);
        return PaymentResponse.from(payment);
    }

    public Page<PaymentResponse> getMyPayments(User customer, Pageable pageable) {
        return paymentRepository.findByCustomer(customer, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional
    public PaymentResponse reportDeposit(User customer, Long paymentId, DepositReportRequest request) {
        Payment payment = paymentRepository.findByIdAndCustomer(paymentId, customer)
                .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다"));

        validateDepositDue(payment);
        payment.reportDeposit(request.depositorName(), request.memo());
        return PaymentResponse.from(payment);
    }

    public Page<PaymentResponse> getPayments(PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = status == null
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findByStatus(status, pageable);
        return payments.map(PaymentResponse::from);
    }

    public PaymentResponse getPaymentForAdmin(Long paymentId) {
        Payment payment = getPaymentEntity(paymentId);
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse confirmDeposit(Long paymentId, PaymentConfirmRequest request) {
        Payment payment = getPaymentEntity(paymentId);
        PaymentConfirmRequest confirmRequest = request == null ? new PaymentConfirmRequest(null, null) : request;

        if (payment.getBooking().getStatus() == BookingStatus.PENDING) {
            payment.confirmDeposit(confirmRequest.confirmedAmount(), confirmRequest.adminMemo());
            Booking booking = payment.getBooking();
            booking.confirmPayment();

            String roomName = counselorProfileRepository.findByUser(booking.getCounselor())
                    .map(profile -> profile.getTitle())
                    .orElseGet(() -> UserNameUtils.fullName(booking.getCounselor()) + " Counseling");
            chatRoomService.createRoom(roomName, booking.getCounselor().getId(), booking.getCustomer().getId(), booking.getId());

            notificationService.notify(booking.getCustomer(), "Booking Confirmed",
                    "Your booking for '" + booking.getTopic() + "' has been confirmed. Your chat room is now open.",
                    NotificationType.BOOKING_CONFIRMED_NOTIFICATION, String.valueOf(booking.getId()));

            return PaymentResponse.from(payment);
        }
        if (payment.getBooking().getStatus() == BookingStatus.PAID) {
            throw new BadRequestException("이미 결제 완료 처리된 예약입니다");
        }
        throw new BadRequestException("결제 확인 가능한 예약 상태가 아닙니다: " + payment.getBooking().getStatus().name());
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, PaymentCancelRequest request) {
        Payment payment = getPaymentEntity(paymentId);
        payment.cancel(request.reason());

        if (payment.getBooking().getStatus() == BookingStatus.PENDING) {
            payment.getBooking().cancel(request.reason());
        }
        return PaymentResponse.from(payment);
    }

    @Transactional
    public Payment createPayment(Booking booking) {
        return buildAndSavePayment(booking, null);
    }

    private PaymentResponse createPayment(Booking booking, String depositorName) {
        return PaymentResponse.from(buildAndSavePayment(booking, depositorName));
    }

    private Payment buildAndSavePayment(Booking booking, String depositorName) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("결제 대기 상태의 예약만 결제를 생성할 수 있습니다");
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .customer(booking.getCustomer())
                .counselor(booking.getCounselor())
                .totalAmount(booking.getTotalPrice())
                .platformFeeRate(bankTransferPaymentProperties.getPlatformFeeRate())
                .bankName(bankTransferPaymentProperties.getBankName())
                .accountNumber(bankTransferPaymentProperties.getAccountNumber())
                .accountHolder(bankTransferPaymentProperties.getAccountHolder())
                .depositDueAt(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(bankTransferPaymentProperties.getDepositTimeoutHours()))
                .depositorName(depositorName)
                .build();

        return paymentRepository.save(payment);
    }

    private Payment getPaymentEntity(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다"));
    }

    private void validateBookingCustomer(User customer, Booking booking) {
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("이 예약의 결제를 생성할 권한이 없습니다");
        }
    }

    private void validatePaymentCustomer(User customer, Payment payment) {
        if (!payment.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("이 결제를 조회할 권한이 없습니다");
        }
    }

    private void validateDepositDue(Payment payment) {
        if (payment.getDepositDueAt().isBefore(LocalDateTime.now(java.time.ZoneOffset.UTC))) {
            throw new BadRequestException("입금 기한이 만료된 결제입니다");
        }
    }
}
