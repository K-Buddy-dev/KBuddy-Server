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
    private String sender; // 발신자 ID

    @Column(nullable = false)
    private String message; // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role; // 발신자 역할 (상담자/내담자)

    @Column(nullable = false)
    private LocalDateTime sentAt; // 전송 시간

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
