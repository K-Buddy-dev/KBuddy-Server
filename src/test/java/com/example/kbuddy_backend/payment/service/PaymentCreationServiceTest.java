package com.example.kbuddy_backend.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.livechat.repository.BookingRepository;
import com.example.kbuddy_backend.payment.config.BankTransferPaymentProperties;
import com.example.kbuddy_backend.payment.entity.Payment;
import com.example.kbuddy_backend.payment.repository.PaymentRepository;
import com.example.kbuddy_backend.payment.dto.response.PaymentResponse;
import com.example.kbuddy_backend.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentCreationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BankTransferPaymentProperties paymentProperties;
    private PaymentCreationService paymentCreationService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        paymentProperties = new BankTransferPaymentProperties();
        paymentProperties.setBankName("KB Bank");
        paymentProperties.setAccountNumber("123-456-789");
        paymentProperties.setAccountHolder("KBuddy");
        paymentProperties.setPlatformFeeRate(new BigDecimal("15.00"));

        paymentCreationService = new PaymentCreationService(
                paymentRepository, bookingRepository, paymentProperties);

        User customer = User.builder().email("customer@test.com").username("customer").build();
        User counselor = User.builder().email("counselor@test.com").username("counselor").build();
        booking = Booking.builder()
                .customer(customer)
                .counselor(counselor)
                .bookingStartUtc(LocalDateTime.now().plusDays(1))
                .bookingEndUtc(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .slotCount(1)
                .totalPrice(20000)
                .build();
        ReflectionTestUtils.setField(booking, "id", 10L);
    }

    @Test
    void returnsExistingPaymentWithoutCreatingAnotherOne() {
        Payment existing = createPayment();
        given(paymentRepository.findByBookingId(10L)).willReturn(Optional.of(existing));

        PaymentResponse result = paymentCreationService.createForCommittedBooking(10L, "Hong Gil Dong");

        assertEquals(existing.getTotalAmount(), result.totalAmount());
        verify(bookingRepository, never()).findById(10L);
        verify(paymentRepository, never()).saveAndFlush(any(Payment.class));
    }

    @Test
    void createsPaymentWhenBookingHasNoPayment() {
        given(paymentRepository.findByBookingId(10L)).willReturn(Optional.empty());
        given(bookingRepository.findById(10L)).willReturn(Optional.of(booking));
        given(paymentRepository.saveAndFlush(any(Payment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse result = paymentCreationService.createForCommittedBooking(10L, "Hong Gil Dong");

        assertEquals(booking.getId(), result.bookingId());
        assertEquals(20000, result.totalAmount());
        assertEquals("Hong Gil Dong", result.depositorName());
    }

    private Payment createPayment() {
        return Payment.builder()
                .booking(booking)
                .customer(booking.getCustomer())
                .counselor(booking.getCounselor())
                .totalAmount(booking.getTotalPrice())
                .platformFeeRate(new BigDecimal("15.00"))
                .bankName("KB Bank")
                .accountNumber("123-456-789")
                .accountHolder("KBuddy")
                .depositDueAt(LocalDateTime.now().plusHours(24))
                .build();
    }
}
