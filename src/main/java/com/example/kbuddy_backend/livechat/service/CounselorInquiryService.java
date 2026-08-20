package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.dto.request.CreateInquiryRequest;
import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.entity.InquiryReply;
import com.example.kbuddy_backend.livechat.repository.CounselorInquiryRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.livechat.repository.InquiryReplyRepository;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.service.NotificationService;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.example.kbuddy_backend.user.util.UserNameUtils;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorInquiryService {

    private final CounselorInquiryRepository inquiryRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final CounselorProfileRepository counselorProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public void createInquiry(User writer, String counselorUuid, CreateInquiryRequest request) {
        CounselorProfile profile = counselorProfileRepository.findByUuid(UUID.fromString(counselorUuid))
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다"));

        CounselorInquiry inquiry = CounselorInquiry.builder()
                .counselor(profile.getUser())
                .writer(writer)
                .title(request.title())
                .content(request.content())
                .isSecret(request.isSecret())
                .build();

        inquiryRepository.save(inquiry);

        notificationService.notify(
                profile.getUser(),
                "New Inquiry Received",
                UserNameUtils.fullName(writer) + " has submitted an inquiry: " + request.title(),
                NotificationType.INQUIRY_NOTIFICATION,
                profile.getUuid().toString());
    }

    public Page<CounselorInquiry> getInquiries(User user, String counselorUuid, Pageable pageable) {
        CounselorProfile profile = counselorProfileRepository.findByUuid(UUID.fromString(counselorUuid))
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다"));
        Long counselorUserId = profile.getUser().getId();

        //비로그인 사용자에게는 공개 문의글만 노출한다.
        if (user == null) {
            return inquiryRepository.findPublicInquiries(counselorUserId, pageable);
        }
        return inquiryRepository.findVisibleInquiries(counselorUserId, user, pageable);
    }

    public CounselorInquiry getInquiryDetail(User user, Long inquiryId) {
        CounselorInquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다"));

        // 비밀글 권한 확인
        //IllegalArgumentException(400)이면 클라이언트가 "로그인 필요"를 구분할 수 없으므로
        //AccessDeniedException으로 던져 익명 401 / 로그인 403으로 내려간다.
        if (!inquiry.canView(user)) {
            throw new AccessDeniedException("이 문의글을 볼 권한이 없습니다");
        }

        return inquiry;
    }

    public List<InquiryReply> getReplies(Long inquiryId) {
        return replyRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId);
    }

    public Set<Long> getInquiryIdsWithReplies(List<Long> inquiryIds) {
        if (inquiryIds.isEmpty()) return Set.of();
        return replyRepository.findInquiryIdsWithReplies(inquiryIds);
    }

    @Transactional
    public void createReply(User user, Long inquiryId, String content) {
        CounselorInquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다"));

        // InquiryReply 생성자에서 권한 검증
        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .user(user)
                .content(content)
                .build();

        replyRepository.save(reply);

        CounselorProfile counselorProfile = counselorProfileRepository.findByUser(inquiry.getCounselor())
                .orElseThrow(() -> new IllegalArgumentException("상담사 프로필을 찾을 수 없습니다"));

        notificationService.notify(
                inquiry.getWriter(),
                "Your Inquiry Has Been Answered",
                UserNameUtils.fullName(user) + " replied to your inquiry: " + inquiry.getTitle(),
                NotificationType.INQUIRY_REPLY_NOTIFICATION,
                counselorProfile.getUuid().toString());
    }
}
