package com.example.kbuddy_backend.payment.service;

import com.example.kbuddy_backend.common.exception.IdempotencyConflictException;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.payment.config.BankTransferPaymentProperties;
import com.example.kbuddy_backend.payment.constant.PaymentStatus;
import com.example.kbuddy_backend.payment.dto.request.BankTransferPaymentCreateRequest;
import com.example.kbuddy_backend.payment.dto.request.DepositReportRequest;
import com.example.kbuddy_backend.payment.dto.request.PaymentConfirmRequest;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;
import com.example.kbuddy_backend.payment.entity.Payment;
import com.example.kbuddy_backend.payment.entity.PaymentIdempotency;
import com.example.kbuddy_backend.payment.repository.PaymentIdempotencyRepository;
import com.example.kbuddy_backend.payment.repository.PaymentRepository;
import com.example.kbuddy_backend.chat.service.ChatRoomService;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.notification.service.NotificationService;
import com.example.kbuddy_backend.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private CounselorProfileRepository counselorProfileRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentCreationService paymentCreationService;

    @Mock
    private PaymentIdempotencyRepository paymentIdempotencyRepository;

    private BankTransferPaymentProperties bankTransferPaymentProperties;
    private PaymentService paymentService;

    private User customer;
    private User counselor;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bankTransferPaymentProperties = new BankTransferPaymentProperties();
        bankTransferPaymentProperties.setBankName("KB Bank");
        bankTransferPaymentProperties.setAccountNumber("123-456-789");
        bankTransferPaymentProperties.setAccountHolder("KBuddy");
        bankTransferPaymentProperties.setDepositTimeoutHours(24);
        bankTransferPaymentProperties.setPlatformFeeRate(new BigDecimal("15.00"));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        PaymentIdempotencyService paymentIdempotencyService =
                new PaymentIdempotencyService(paymentIdempotencyRepository, objectMapper);
        paymentService = new PaymentService(
                paymentRepository,
                bookingRepository,
                bankTransferPaymentProperties,
                chatRoomService,
                counselorProfileRepository,
                notificationService,
                paymentCreationService,
                paymentIdempotencyService);

        customer = User.builder()
                .email("customer@kbuddy.com")
                .username("customer")
                .build();
        ReflectionTestUtils.setField(customer, "id", 1L);

        counselor = User.builder()
                .email("counselor@kbuddy.com")
                .username("counselor")
                .build();
        ReflectionTestUtils.setField(counselor, "id", 2L);

        booking = Booking.builder()
                .customer(customer)
                .counselor(counselor)
                .bookingStartUtc(LocalDateTime.of(2026, 5, 25, 10, 0))
                .bookingEndUtc(LocalDateTime.of(2026, 5, 25, 10, 30))
                .slotCount(1)
                .totalPrice(20000)
                .build();
        ReflectionTestUtils.setField(booking, "id", 10L);
    }

    @Test
    @DisplayName("무통장 입금 결제를 생성하면 입금 대기 상태와 수수료 스냅샷을 저장한다")
    void createBankTransferPayment() {
        given(bookingRepository.findById(10L)).willReturn(Optional.of(booking));
        given(paymentRepository.findByBookingId(10L)).willReturn(Optional.empty());
        given(paymentCreationService.createForCommittedBooking(10L, "Hong Gil Dong"))
                .willReturn(PaymentResponse.from(createPayment()));

        PaymentResponse response = paymentService.createBankTransferPayment(
                customer,
                new BankTransferPaymentCreateRequest(10L, "Hong Gil Dong")
        );

        assertEquals(100L, response.paymentId());
        assertEquals(10L, response.bookingId());
        assertEquals(PaymentStatus.AWAITING_DEPOSIT, response.status());
        assertEquals(20000, response.totalAmount());
        assertEquals(new BigDecimal("15.00"), response.platformFeeRate());
        assertEquals(3000, response.platformFeeAmount());
        assertEquals(17000, response.counselorSettlementAmount());
        assertEquals("KB Bank", response.bankName());
        assertEquals("123-456-789", response.accountNumber());
        assertEquals("KBuddy", response.accountHolder());
        assertEquals("Hong Gil Dong", response.depositorName());
        assertNotNull(response.depositDueAt());
    }

    @Test
    @DisplayName("예약자가 아닌 사용자는 결제를 생성할 수 없다")
    void createBankTransferPaymentByOtherUser() {
        User otherUser = User.builder()
                .email("other@kbuddy.com")
                .username("other")
                .build();
        ReflectionTestUtils.setField(otherUser, "id", 99L);

        given(bookingRepository.findById(10L)).willReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () ->
                paymentService.createBankTransferPayment(
                        otherUser,
                        new BankTransferPaymentCreateRequest(10L, "Other")
                )
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("동시 결제 생성 충돌이 발생하면 기존 결제를 반환한다")
    void createBankTransferPaymentReturnsExistingPaymentAfterUniqueConflict() {
        Payment existingPayment = createPayment();
        given(bookingRepository.findById(10L)).willReturn(Optional.of(booking));
        given(paymentRepository.findByBookingId(10L))
                .willReturn(Optional.empty(), Optional.of(existingPayment));
        given(paymentCreationService.createForCommittedBooking(10L, "Hong Gil Dong"))
                .willThrow(paymentBookingUniqueConflict());

        PaymentResponse response = paymentService.createBankTransferPayment(
                customer,
                new BankTransferPaymentCreateRequest(10L, "Hong Gil Dong"));

        assertEquals(100L, response.paymentId());
    }

    @Test
    @DisplayName("결제 생성 중 유니크 충돌이 아닌 DB 오류는 그대로 전파한다")
    void createBankTransferPaymentDoesNotHideOtherDatabaseErrors() {
        given(bookingRepository.findById(10L)).willReturn(Optional.of(booking));
        given(paymentRepository.findByBookingId(10L)).willReturn(Optional.empty());
        DataIntegrityViolationException databaseError =
                new DataIntegrityViolationException("bank name is null");
        given(paymentCreationService.createForCommittedBooking(10L, "Hong Gil Dong"))
                .willThrow(databaseError);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentService.createBankTransferPayment(
                        customer,
                        new BankTransferPaymentCreateRequest(10L, "Hong Gil Dong")));

        assertEquals(databaseError, thrown);
    }

    @Test
    @DisplayName("내담자가 입금 완료를 신고하면 입금 완료 신고 상태가 된다")
    void reportDeposit() {
        Payment payment = createPayment();
        given(paymentRepository.findByIdAndCustomer(100L, customer)).willReturn(Optional.of(payment));

        PaymentResponse response = paymentService.reportDeposit(
                customer,
                100L,
                new DepositReportRequest("Hong Gil Dong", "입금했습니다")
        );

        assertEquals(PaymentStatus.DEPOSIT_REPORTED, response.status());
        assertEquals("Hong Gil Dong", response.depositorName());
        assertEquals("입금했습니다", response.customerMemo());
        assertNotNull(response.depositReportedAt());
    }

    @Test
    @DisplayName("같은 입금 신고를 다시 보내면 최초 신고 결과를 반환한다")
    void reportDepositRetryReturnsExistingResult() {
        Payment payment = createPayment();
        given(paymentRepository.findByIdAndCustomer(100L, customer)).willReturn(Optional.of(payment));
        DepositReportRequest request = new DepositReportRequest("Hong Gil Dong", "입금했습니다");

        PaymentResponse first = paymentService.reportDeposit(customer, 100L, request);
        PaymentResponse second = paymentService.reportDeposit(customer, 100L, request);

        assertEquals(first.depositReportedAt(), second.depositReportedAt());
        assertEquals(PaymentStatus.DEPOSIT_REPORTED, second.status());
    }

    @Test
    @DisplayName("입금 신고 후 다른 정보로 재요청하면 충돌 처리한다")
    void reportDepositRetryWithDifferentDataReturnsConflict() {
        Payment payment = createPayment();
        given(paymentRepository.findByIdAndCustomer(100L, customer)).willReturn(Optional.of(payment));
        paymentService.reportDeposit(
                customer, 100L, new DepositReportRequest("Hong Gil Dong", "입금했습니다"));

        assertThrows(IdempotencyConflictException.class, () ->
                paymentService.reportDeposit(
                        customer, 100L, new DepositReportRequest("Different Name", "수정")));
    }

    @Test
    @DisplayName("관리자가 입금을 확인하면 결제와 예약이 모두 결제 완료 상태가 된다")
    void confirmDeposit() {
        Payment payment = createPayment();
        payment.reportDeposit("Hong Gil Dong", "입금했습니다");
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));
        stubIdempotencyRepository();
        given(chatRoomService.findOrCreateRoom(any(), any(), any(), any()))
                .willReturn("room-1");

        PaymentResponse response = paymentService.confirmDeposit(
                100L,
                new PaymentConfirmRequest(20000, "관리자 확인"),
                UUID.randomUUID()
        );

        assertEquals(PaymentStatus.PAID, response.status());
        assertEquals(20000, response.confirmedAmount());
        assertEquals("관리자 확인", response.adminMemo());
        assertNotNull(response.paidAt());
        assertEquals(BookingStatus.PAID, booking.getStatus());
    }

    @Test
    @DisplayName("같은 멱등키로 결제 승인을 재요청하면 저장된 성공 결과를 반환한다")
    void confirmDepositRetryReturnsStoredResponse() {
        Payment payment = createPayment();
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));
        AtomicReference<PaymentIdempotency> stored = stubIdempotencyRepository();
        given(chatRoomService.findOrCreateRoom(any(), any(), any(), any()))
                .willReturn("room-1");
        UUID idempotencyKey = UUID.randomUUID();
        PaymentConfirmRequest request = new PaymentConfirmRequest(20000, "관리자 확인");

        PaymentResponse first = paymentService.confirmDeposit(100L, request, idempotencyKey);
        PaymentResponse second = paymentService.confirmDeposit(100L, request, idempotencyKey);

        assertEquals(first, second);
        assertNotNull(stored.get().getResponseBody());
        verify(chatRoomService, times(1))
                .findOrCreateRoom(any(), any(), any(), any());
        verify(notificationService, times(2))
                .notifyOnce(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("같은 멱등키를 다른 승인 요청에 사용하면 충돌 처리한다")
    void confirmDepositRetryWithDifferentRequestReturnsConflict() {
        Payment payment = createPayment();
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));
        stubIdempotencyRepository();
        given(chatRoomService.findOrCreateRoom(any(), any(), any(), any()))
                .willReturn("room-1");
        UUID idempotencyKey = UUID.randomUUID();

        paymentService.confirmDeposit(
                100L, new PaymentConfirmRequest(20000, "관리자 확인"), idempotencyKey);

        assertThrows(IdempotencyConflictException.class, () ->
                paymentService.confirmDeposit(
                        100L,
                        new PaymentConfirmRequest(19000, "다른 요청"),
                        idempotencyKey));
    }

    @Test
    @DisplayName("이미 승인된 결제를 같은 금액과 새 멱등키로 요청하면 기존 성공 결과를 반환한다")
    void alreadyPaidPaymentReturnsExistingSuccessForNewKey() {
        Payment payment = createPayment();
        payment.confirmDeposit(20000, "최초 승인");
        booking.confirmPayment();
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));
        stubIdempotencyRepository();

        PaymentResponse response = paymentService.confirmDeposit(
                100L,
                new PaymentConfirmRequest(20000, "재요청"),
                UUID.randomUUID());

        assertEquals(PaymentStatus.PAID, response.status());
        assertEquals(20000, response.confirmedAmount());
        verify(chatRoomService, never()).findOrCreateRoom(any(), any(), any(), any());
        verify(notificationService, never()).notifyOnce(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("이미 승인된 결제를 다른 금액으로 요청하면 충돌 처리한다")
    void alreadyPaidPaymentRejectsDifferentAmount() {
        Payment payment = createPayment();
        payment.confirmDeposit(20000, "최초 승인");
        booking.confirmPayment();
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));
        stubIdempotencyRepository();

        assertThrows(IdempotencyConflictException.class, () ->
                paymentService.confirmDeposit(
                        100L,
                        new PaymentConfirmRequest(19000, "다른 금액"),
                        UUID.randomUUID()));
    }

    @Test
    @DisplayName("입금 확인 금액이 결제 금액과 다르면 결제 완료 처리하지 않는다")
    void confirmDepositWithWrongAmount() {
        Payment payment = createPayment();
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));
        stubIdempotencyRepository();

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.confirmDeposit(
                        100L,
                        new PaymentConfirmRequest(19000, "금액 불일치"),
                        UUID.randomUUID()
                )
        );

        assertEquals(PaymentStatus.AWAITING_DEPOSIT, payment.getStatus());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }

    private Payment createPayment() {
        Payment payment = Payment.builder()
                .booking(booking)
                .customer(customer)
                .counselor(counselor)
                .totalAmount(booking.getTotalPrice())
                .platformFeeRate(new BigDecimal("15.00"))
                .bankName("KB Bank")
                .accountNumber("123-456-789")
                .accountHolder("KBuddy")
                .depositDueAt(LocalDateTime.now().plusHours(24))
                .depositorName("Hong Gil Dong")
                .build();
        ReflectionTestUtils.setField(payment, "id", 100L);
        return payment;
    }

    private AtomicReference<PaymentIdempotency> stubIdempotencyRepository() {
        AtomicReference<PaymentIdempotency> stored = new AtomicReference<>();
        given(paymentIdempotencyRepository.findByIdempotencyKey(any()))
                .willAnswer(invocation -> Optional.ofNullable(stored.get()));
        given(paymentIdempotencyRepository.saveAndFlush(any(PaymentIdempotency.class)))
                .willAnswer(invocation -> {
                    PaymentIdempotency idempotency = invocation.getArgument(0);
                    stored.set(idempotency);
                    return idempotency;
                });
        return stored;
    }

    private DataIntegrityViolationException paymentBookingUniqueConflict() {
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
                "booking_id conflict",
                new SQLException("duplicate key"),
                "uk_payment_booking");
        return new DataIntegrityViolationException("booking_id conflict", constraintViolation);
    }
}
