package com.example.kbuddy_backend.payment.service;

import com.example.kbuddy_backend.chat.service.ChatRoomService;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.IdempotencyConflictException;
import com.example.kbuddy_backend.common.exception.NotFoundException;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.payment.config.BankTransferPaymentProperties;
import com.example.kbuddy_backend.payment.constant.PaymentIdempotencyOperation;
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
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String PAYMENT_BOOKING_UNIQUE_CONSTRAINT = "uk_payment_booking";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BankTransferPaymentProperties bankTransferPaymentProperties;
    private final ChatRoomService chatRoomService;
    private final CounselorProfileRepository counselorProfileRepository;
    private final NotificationService notificationService;
    private final PaymentCreationService paymentCreationService;
    private final PaymentIdempotencyService paymentIdempotencyService;

    @Transactional
    public PaymentResponse createBankTransferPayment(User customer, BankTransferPaymentCreateRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다"));

        validateBookingCustomer(customer, booking);

        return paymentRepository.findByBookingId(booking.getId())
                .map(PaymentResponse::from)
                .orElseGet(() -> createPaymentSafely(booking.getId(), request.depositorName()));
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

        // 이미 접수된 신고와 내용이 같으면 재시도로 보고 기존 결과를 반환한다.
        // 내용이 다르면 같은 작업을 다른 값으로 변경하려는 요청이므로 충돌로 처리한다.
        if (payment.getStatus() == PaymentStatus.DEPOSIT_REPORTED) {
            if (Objects.equals(payment.getDepositorName(), request.depositorName())
                    && Objects.equals(payment.getCustomerMemo(), request.memo())) {
                return PaymentResponse.from(payment);
            }
            throw new IdempotencyConflictException(
                    "이미 신고된 입금 정보와 다른 요청입니다.");
        }

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
    public PaymentResponse confirmDeposit(
            Long paymentId,
            PaymentConfirmRequest request,
            UUID idempotencyKey) {
        Payment payment = getPaymentEntity(paymentId);
        PaymentConfirmRequest confirmRequest = request == null ? new PaymentConfirmRequest(null, null) : request;
        // 금액을 생략한 요청과 실제 결제 금액을 명시한 요청이 같은 의미가 되도록 먼저 정규화한다.
        int amountToConfirm = confirmRequest.confirmedAmount() == null
                ? payment.getTotalAmount()
                : confirmRequest.confirmedAmount();
        String requestHash = paymentIdempotencyService.createConfirmRequestHash(
                paymentId, amountToConfirm, confirmRequest.adminMemo());
        PaymentIdempotencyService.StartResult idempotency = paymentIdempotencyService.start(
                idempotencyKey.toString(),
                PaymentIdempotencyOperation.CONFIRM_DEPOSIT,
                payment,
                requestHash);

        // 같은 멱등키로 완료된 요청은 상태 변경과 후속 작업을 반복하지 않고 최초 응답을 재사용한다.
        if (idempotency.isCompleted()) {
            return idempotency.completedResponse();
        }

        // 결제가 이미 승인됐어도 요청 값이 기존 승인과 같다면 새로운 멱등키를 성공 결과로 수렴시킨다.
        if (payment.getStatus() == PaymentStatus.PAID) {
            if (payment.getBooking().getStatus() != BookingStatus.PAID) {
                throw new BadRequestException("결제와 예약 상태가 일치하지 않습니다.");
            }
            int existingConfirmedAmount = payment.getConfirmedAmount() == null
                    ? payment.getTotalAmount()
                    : payment.getConfirmedAmount();
            if (amountToConfirm != existingConfirmedAmount) {
                throw new IdempotencyConflictException(
                        "이미 승인된 결제 금액과 다른 요청입니다.");
            }
            PaymentResponse response = PaymentResponse.from(payment);
            paymentIdempotencyService.complete(idempotency.idempotency(), response);
            return response;
        }

        if (payment.getBooking().getStatus() == BookingStatus.PENDING) {
            payment.confirmDeposit(confirmRequest.confirmedAmount(), confirmRequest.adminMemo());
            Booking booking = payment.getBooking();
            booking.confirmPayment();

            String roomName = counselorProfileRepository.findByUser(booking.getCounselor())
                    .map(profile -> profile.getTitle())
                    .orElseGet(() -> UserNameUtils.fullName(booking.getCounselor()) + " Counseling");
            // 결제 승인 재시도에서도 예약별 채팅방은 한 번만 생성되도록 기존 방을 우선 조회한다.
            String roomId = chatRoomService.findOrCreateRoom(
                    roomName,
                    booking.getCounselor().getId(),
                    booking.getCustomer().getId(),
                    booking.getId());

            // 수신자별 이벤트 키를 분리해 고객과 상담사 알림은 각각 한 번씩만 발송한다.
            notificationService.notifyOnce(
                    "PAYMENT_CONFIRMED:" + payment.getId() + ":CUSTOMER",
                    booking.getCustomer(), "Chat Room Ready",
                    "Your booking for '" + booking.getTopic() + "' has been confirmed. Your chat room is now open.",
                    NotificationType.CHAT_MESSAGE_NOTIFICATION, roomId);

            notificationService.notifyOnce(
                    "PAYMENT_CONFIRMED:" + payment.getId() + ":COUNSELOR",
                    booking.getCounselor(), "Chat Room Ready",
                    "A booking from " + UserNameUtils.fullName(booking.getCustomer()) + " for '" + booking.getTopic() + "' has been confirmed. Your chat room is now open.",
                    NotificationType.CHAT_MESSAGE_NOTIFICATION, roomId);

            PaymentResponse response = PaymentResponse.from(payment);
            paymentIdempotencyService.complete(idempotency.idempotency(), response);
            return response;
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

    private PaymentResponse createPaymentSafely(Long bookingId, String depositorName) {
        try {
            // 별도 트랜잭션에서 생성해 유니크 충돌이 발생해도 실패한 INSERT만 먼저 롤백되게 한다.
            return paymentCreationService.createForCommittedBooking(bookingId, depositorName);
        } catch (DataIntegrityViolationException exception) {
            if (!isPaymentBookingUniqueConflict(exception)) {
                throw exception;
            }
            // 동시에 먼저 생성된 결제가 있으면 중복 실패 대신 그 결제를 조회해 동일한 성공 결과를 반환한다.
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .orElseThrow(() -> exception);
            return PaymentResponse.from(payment);
        }
    }

    private boolean isPaymentBookingUniqueConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            // Hibernate가 제공하는 이름을 우선 확인해 예약-결제 유니크 충돌만 복구 대상으로 삼는다.
            if (cause instanceof ConstraintViolationException constraintViolation) {
                String constraintName = constraintViolation.getConstraintName();
                if (constraintName != null
                        && PAYMENT_BOOKING_UNIQUE_CONSTRAINT.equalsIgnoreCase(constraintName)) {
                    return true;
                }
            }
            // PostgreSQL 유니크 위반 코드도 확인해 드라이버가 제약조건 이름을 주지 않는 경우를 보완한다.
            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
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
