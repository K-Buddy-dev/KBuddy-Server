package com.example.kbuddy_backend.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    // 메시지 타입: 입장, 퇴장, 채팅
    public enum MessageType {
        ENTER, LEAVE, CHAT
    }

    private MessageType type; // 메시지 타입
    private String roomId;    // 채팅방 ID
    private String sender;    // 메시지 발신자
    private String message;   // 메시지 내용
    private String role;      // 발신자 역할 (상담자/내담자)
}
