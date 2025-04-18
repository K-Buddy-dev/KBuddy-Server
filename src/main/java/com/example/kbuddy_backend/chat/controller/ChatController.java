package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.dto.ChatMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat API", description = "웹소캣 채팅 관련 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    @Operation(summary = "채팅방 설정", description = "새로운 채팅방을 생성합니다.")
    @PostMapping("/rooms")
    public String createRoom(@RequestParam String roomId) {
        return "채팅방이 생성되었습니다. roomId = " + roomId;
    }

    @Operation(summary = "채팅방 입장", description = "특정 채팅방에 입장합니다.")
    @PostMapping("/rooms/{roomId}/join")
    public String joinRoom(@PathVariable String roomId, @RequestParam String username) {
        return username + "님이" + roomId + " 채팅방에 입장하셨습니다.";
    }

    @Operation(summary = "채팅 메시지 전송", description = "웹소켓을 통해 채팅 메시지를 전송합니다.")
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @Operation(summary = "사용자 추가", description = "채팅방에 새로운 사용자를 추가합니다.")
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // 웹소켓 세션에 사용자 이름 저장
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }
}
