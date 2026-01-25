package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.dto.request.CreateInquiryRequest;
import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.livechat.entity.InquiryReply;
import com.example.kbuddy_backend.livechat.repository.CounselorInquiryRepository;
import com.example.kbuddy_backend.livechat.repository.InquiryReplyRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorInquiryService {

    private final CounselorInquiryRepository inquiryRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createInquiry(User writer, Long counselorId, CreateInquiryRequest request) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다"));

        CounselorInquiry inquiry = CounselorInquiry.builder()
                .counselor(counselor)
                .writer(writer)
                .title(request.title())
                .content(request.content())
                .isSecret(request.isSecret())
                .build();

        inquiryRepository.save(inquiry);
    }

    public Page<CounselorInquiry> getInquiries(User user, Long counselorId, Pageable pageable) {
        return inquiryRepository.findVisibleInquiries(counselorId, user, pageable);
    }

    public CounselorInquiry getInquiryDetail(User user, Long inquiryId) {
        CounselorInquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다"));

        // 비밀글 권한 확인
        if (!inquiry.canView(user)) {
            throw new IllegalArgumentException("이 문의글을 볼 권한이 없습니다");
        }

        return inquiry;
    }

    public List<InquiryReply> getReplies(Long inquiryId) {
        return replyRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId);
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
    }
}
