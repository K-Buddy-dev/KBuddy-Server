package com.example.kbuddy_backend.chat.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoomSummary {
    private final String roomId;
    private final String roomName;
    private final Long peerUserId;
    private final String peerNickname;
    private final String peerProfileImageUrl;
    private final String lastMessage;
    private final LocalDateTime lastMessageAt;
    private final long unreadCount;
}
