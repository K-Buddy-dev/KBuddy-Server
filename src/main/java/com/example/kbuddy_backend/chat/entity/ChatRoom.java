package com.example.kbuddy_backend.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomId; // 채팅방 고유 ID, UUID를 사용하기 위해 String

    @Column(nullable = false)
    private String name; // 채팅방 이름

    @Column(nullable = false)
    private Long counselorId; // 상담자 ID

    @Column(nullable = false)
    private Long clientId; // 내담자 ID

    @Column(unique = true)
    private Long bookingId; // 연결된 예약 ID

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성 시간

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();  // 채팅 메시지 목록

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

}
