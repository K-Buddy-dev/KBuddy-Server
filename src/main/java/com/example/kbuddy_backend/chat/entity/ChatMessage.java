package com.example.kbuddy_backend.chat.entity;

import com.example.kbuddy_backend.chat.constant.ChatRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 채팅방

    @Column(nullable = false)
    private Long sender; // 발신자 ID

    @Column(nullable = false)
    private String message; // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role; // 발신자 역할 (상담자/내담자)

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(length = 64)
    private String clientMessageId; // 클라이언트 임시 ID (중복 방지 및 echo 매칭용)

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}
