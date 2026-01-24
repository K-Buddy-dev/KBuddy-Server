package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "counselor_inquiry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorInquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isSecret = false;

    @Builder
    public CounselorInquiry(User counselor, User writer, String title, String content, boolean isSecret) {
        this.counselor = counselor;
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.isSecret = isSecret;
    }

    public boolean canView(User user) {
        if (!this.isSecret) {
            return true;
        }
        return user.getId().equals(this.writer.getId()) ||
                user.getId().equals(this.counselor.getId());
    }
}
