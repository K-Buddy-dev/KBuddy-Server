package com.example.kbuddy_backend.chat.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    // 메시지 타입: 입장, 퇴장, 채팅
    public enum MessageType {
        JOIN ,ENTER, LEAVE, TALK
    }

    private MessageType messageType;
    private String roomId;
    private String sender;
    private String message;
    private String role;
    private LocalDateTime sentAt;
    private String clientMessageId; // 클라이언트 임시 ID (echo 매칭용)
}
