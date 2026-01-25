package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.request.CreateInquiryRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateReviewRequest;
import com.example.kbuddy_backend.livechat.service.CounselorInquiryService;
import com.example.kbuddy_backend.livechat.service.CounselorReviewService;
import com.example.kbuddy_backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1")
@Tag(name = "Review & Inquiry API", description = "상담 리뷰 및 문의 API")
public class CounselorReviewController {

    private final CounselorReviewService counselorReviewService;
    private final CounselorInquiryService counselorInquiryService;

    @PostMapping("/review")
    @Operation(summary = "리뷰 작성", description = "상담 완료 후 상담사에 대한 리뷰를 작성합니다.")
    public ResponseEntity<Void> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        counselorReviewService.createReview(user, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/counselor/{counselorId}/inquiry")
    @Operation(summary = "문의글 작성", description = "상담사에게 문의글을 작성합니다. 비밀글로 설정할 수 있습니다.")
    public ResponseEntity<Void> createInquiry(
            @PathVariable Long counselorId,
            @Valid @RequestBody CreateInquiryRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        counselorInquiryService.createInquiry(user, counselorId, request);
        return ResponseEntity.noContent().build();
    }
}
