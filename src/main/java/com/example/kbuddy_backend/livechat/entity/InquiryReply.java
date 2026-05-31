package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_reply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private CounselorInquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    @Builder
    public InquiryReply(CounselorInquiry inquiry, User user, String content) {
        validateReplyPermission(inquiry, user);
        this.inquiry = inquiry;
        this.user = user;
        this.content = content;
    }

    private void validateReplyPermission(CounselorInquiry inquiry, User user) {
        boolean isWriter = user.getId().equals(inquiry.getWriter().getId());
        boolean isCounselor = user.getId().equals(inquiry.getCounselor().getId());
        if (!isWriter && !isCounselor) {
            throw new IllegalArgumentException("작성자 또는 상담사만 답변할 수 있습니다");
        }
    }
}
