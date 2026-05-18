package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.request.CreateAvailabilityBulkRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateAvailabilityRequest;
import com.example.kbuddy_backend.livechat.dto.response.CounselorAvailabilityResponse;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.service.CounselorAvailabilityService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/counselor/availability")
@Tag(name = "Counselor Availability API", description = "상담 가능 시간 관리 API")
public class CounselorAvailabilityController {

    private final CounselorAvailabilityService availabilityService;

    @PostMapping
    @Operation(summary = "상담 가능 시간 추가", description = "로그인한 상담사의 상담 가능 시간 슬롯을 1개 추가합니다.")
    public ResponseEntity<CounselorAvailabilityResponse.AvailabilitySlot> createAvailability(
            @Valid @RequestBody CreateAvailabilityRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        CounselorAvailability availability = availabilityService.addAvailability(
                user, request.date(), request.startTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(availability));
    }

    @PostMapping("/bulk")
    @Operation(summary = "상담 가능 시간 일괄 추가", description = "로그인한 상담사의 특정 날짜 시작~종료 시간 범위에 30분 단위 슬롯을 추가합니다.")
    public ResponseEntity<CounselorAvailabilityResponse> createAvailabilityBulk(
            @Valid @RequestBody CreateAvailabilityBulkRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        List<CounselorAvailability> availabilities = availabilityService.addAvailabilityBulk(
                user, request.date(), request.startTime(), request.endTime());

        List<CounselorAvailabilityResponse.AvailabilitySlot> slots = availabilities.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(new CounselorAvailabilityResponse(slots));
    }

    private CounselorAvailabilityResponse.AvailabilitySlot toResponse(CounselorAvailability availability) {
        return new CounselorAvailabilityResponse.AvailabilitySlot(
                availability.getId(),
                availability.getSlotDate(),
                availability.getSlotStartTime(),
                availability.getStatus().name());
    }
}
