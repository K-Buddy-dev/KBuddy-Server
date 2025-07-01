package com.example.kbuddy_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@AllArgsConstructor
@Builder
public class ChatRoom {
    private final String roomId;
    private final String name;

    public static ChatRoom of(String name) {
        return ChatRoom.builder()
                .name(name)
                .roomId(UUID.randomUUID().toString())
                .build();
    }
}
