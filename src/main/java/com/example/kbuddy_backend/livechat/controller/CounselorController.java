package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.request.RegisterCounselorRequest;
import com.example.kbuddy_backend.livechat.dto.request.UpdateCounselorRequest;
import com.example.kbuddy_backend.livechat.dto.response.CounselorAvailabilityResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorDetailResponse;
import com.example.kbuddy_backend.livechat.dto.response.CounselorListResponse;
import com.example.kbuddy_backend.livechat.service.CounselorProfileService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상담사 프로필 등록", description = "상담사 프로필을 등록합니다. 커버 이미지, 자격증 파일, 추가 사진을 multipart로 함께 전송합니다.")
    public ResponseEntity<Void> registerCounselor(
            @Valid @RequestPart("data") RegisterCounselorRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "proofFile", required = false) MultipartFile proofFile,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @Parameter(hidden = true) @CurrentUser User user) {
        counselorProfileService.registerCounselor(user, request, coverImage, proofFile, photos);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상담사 프로필 수정", description = "상담사 프로필을 수정합니다. 변경할 필드만 포함합니다.")
    public ResponseEntity<Void> updateCounselor(
            @Valid @RequestPart("data") UpdateCounselorRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "proofFile", required = false) MultipartFile proofFile,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @Parameter(hidden = true) @CurrentUser User user) {
        counselorProfileService.updateCounselor(user, request, coverImage, proofFile, photos);
        return ResponseEntity.noContent().build();
    }
}
