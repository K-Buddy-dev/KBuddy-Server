package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.response.CounselorAvailabilityResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorDetailResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorListResponse;
import com.example.kbuddy_backend.livechat.service.CounselorProfileService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/counselor")
@Tag(name = "Counselor API", description = "상담사 조회 API")
public class CounselorController {

    private final CounselorProfileService counselorProfileService;

    @GetMapping
    @Operation(summary = "상담사 목록 조회", description = "상담사 목록을 페이징하여 조회합니다.")
    public ResponseEntity<CounselorListResponse> getCounselors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @Parameter(hidden = true) @CurrentUser User user) {
        Pageable pageable = PageRequest.of(page, size);
        CounselorListResponse response = counselorProfileService.getCounselors(sort, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{counselorId}")
    @Operation(summary = "상담사 상세 조회", description = "상담사 프로필 상세 정보와 최근 리뷰를 조회합니다.")
    public ResponseEntity<CounselorDetailResponse> getCounselor(
            @PathVariable Long counselorId,
            @Parameter(hidden = true) @CurrentUser User user) {
        CounselorDetailResponse response = counselorProfileService.getCounselor(counselorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{counselorId}/availability")
    @Operation(summary = "상담 가능 시간 조회", description = "특정 월의 상담 가능 시간 슬롯을 조회합니다.")
    public ResponseEntity<CounselorAvailabilityResponse> getAvailability(
            @PathVariable Long counselorId,
            @RequestParam int year,
            @RequestParam int month,
            @Parameter(hidden = true) @CurrentUser User user) {
        CounselorAvailabilityResponse response = counselorProfileService.getAvailability(counselorId, year, month);
        return ResponseEntity.ok(response);
    }
}
