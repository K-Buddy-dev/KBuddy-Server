package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.response.BookingListResponse;
import com.example.kbuddy_backend.livechat.dto.response.BookingReserveResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorBookingListResponse;
import com.example.kbuddy_backend.livechat.service.BookingService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/booking")
@Tag(name = "Booking API", description = "상담 예약 API")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    @Operation(summary = "예약 선점", description = "상담 시간을 선점하고 결제 대기 상태로 전환합니다. 일정 시간 내 결제하지 않으면 자동 해제됩니다.")
    public ResponseEntity<BookingReserveResponse> reserveBooking(
            @Valid @RequestBody BookingReserveRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        BookingReserveResponse response = bookingService.reserve(user, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bookingId}/confirm")
    @Operation(summary = "결제 확인", description = "예약을 결제 완료 상태로 전환합니다.")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long bookingId) {
        bookingService.confirmPayment(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{bookingId}/complete")
    @Operation(summary = "상담 완료", description = "상담 완료 처리합니다.")
    public ResponseEntity<Void> completeBooking(@PathVariable Long bookingId) {
        bookingService.completeBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "예약 취소", description = "예약을 취소합니다.")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true) @CurrentUser User user) {
        bookingService.cancelBooking(user, bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자(내담자)의 예약 목록을 조회합니다.")
    public ResponseEntity<BookingListResponse> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentUser User user) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getMyBookings(user, pageable));
    }

    @GetMapping("/counselor")
    @Operation(summary = "상담사 주문 목록 조회", description = "로그인한 상담사에게 들어온 예약 목록을 조회합니다.")
    public ResponseEntity<CounselorBookingListResponse> getCounselorBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentUser User user) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getCounselorBookings(user, pageable));
    }
}
