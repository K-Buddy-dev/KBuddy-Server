package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.request.BookingReserveRequest;
import com.example.kbuddy_backend.livechat.dto.response.BookingReserveResponse;
import com.example.kbuddy_backend.livechat.service.BookingService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
