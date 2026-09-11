package com.example.kbuddy_backend.livechat.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.livechat.dto.request.CreateInquiryRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateReplyRequest;
import com.example.kbuddy_backend.livechat.dto.request.CreateReviewRequest;
import com.example.kbuddy_backend.livechat.dto.response.InquiryDetailResponse;
import com.example.kbuddy_backend.livechat.dto.response.InquiryListResponse;
import com.example.kbuddy_backend.livechat.dto.response.ReviewListResponse;
import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.livechat.entity.InquiryReply;
import com.example.kbuddy_backend.livechat.service.CounselorInquiryService;
import com.example.kbuddy_backend.livechat.service.CounselorReviewService;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.util.UserNameUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/counselor/{counselorId}/review")
    @Operation(summary = "리뷰 목록 조회", description = "상담사의 리뷰 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ReviewListResponse> getReviews(
            @PathVariable String counselorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @CurrentUser(required = false) User user) {
        ReviewListResponse response = counselorReviewService.getReviews(counselorId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/counselor/{counselorId}/inquiry")
    @Operation(summary = "문의글 작성", description = "상담사에게 문의글을 작성합니다. 비밀글로 설정할 수 있습니다.")
    public ResponseEntity<Void> createInquiry(
            @PathVariable String counselorId,
            @Valid @RequestBody CreateInquiryRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        counselorInquiryService.createInquiry(user, counselorId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/counselor/{counselorId}/inquiry")
    @Operation(summary = "문의글 목록 조회", description = "상담사의 문의글 목록을 조회합니다. 비밀글은 작성자와 상담사만 볼 수 있습니다.")
    public ResponseEntity<InquiryListResponse> getInquiries(
            @PathVariable String counselorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @CurrentUser(required = false) User user) {
        var inquiries = counselorInquiryService.getInquiries(user, counselorId, PageRequest.of(page, size));
        List<Long> inquiryIds = inquiries.getContent().stream().map(i -> i.getId()).toList();
        var repliedIds = counselorInquiryService.getInquiryIdsWithReplies(inquiryIds);
        List<InquiryListResponse.InquiryItem> items = inquiries.getContent().stream()
                .map(i -> new InquiryListResponse.InquiryItem(
                        i.getId(),
                        i.getTitle(),
                        UserNameUtils.fullName(i.getWriter()),
                        i.isSecret(),
                        repliedIds.contains(i.getId()),
                        i.getCreatedDate()))
                .toList();
        return ResponseEntity.ok(new InquiryListResponse(items, inquiries.getTotalElements()));
    }

    @GetMapping("/counselor/{counselorId}/inquiry/{inquiryId}")
    @Operation(summary = "문의글 상세 조회", description = "문의글 상세 내용과 답글 목록을 조회합니다.")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @PathVariable String counselorId,
            @PathVariable Long inquiryId,
            @Parameter(hidden = true) @CurrentUser(required = false) User user) {
        CounselorInquiry inquiry = counselorInquiryService.getInquiryDetail(user, inquiryId);
        List<InquiryReply> replies = counselorInquiryService.getReplies(inquiryId);

        List<InquiryDetailResponse.ReplyItem> replyItems = replies.stream()
                .map(r -> new InquiryDetailResponse.ReplyItem(
                        r.getId(),
                        UserNameUtils.fullName(r.getUser()),
                        r.getContent(),
                        r.getCreatedAt()))
                .toList();

        InquiryDetailResponse response = new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                UserNameUtils.fullName(inquiry.getWriter()),
                inquiry.isSecret(),
                inquiry.getCreatedDate(),
                replyItems);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/counselor/{counselorId}/inquiry/{inquiryId}/reply")
    @Operation(summary = "답글 작성", description = "문의글에 답글을 작성합니다. 작성자와 상담사만 작성 가능합니다.")
    public ResponseEntity<Void> createReply(
            @PathVariable String counselorId,
            @PathVariable Long inquiryId,
            @Valid @RequestBody CreateReplyRequest request,
            @Parameter(hidden = true) @CurrentUser User user) {
        counselorInquiryService.createReply(user, inquiryId, request.content());
        return ResponseEntity.noContent().build();
    }
}
