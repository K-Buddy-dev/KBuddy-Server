package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.base.redis.service.RedisPublisher;
import com.example.kbuddy_backend.chat.dto.ChatMessage;
import com.example.kbuddy_backend.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat API", description = "웹소캣 채팅 관련 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatRoomService chatRoomService;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessage message) {
        if (isJoin(message)) {
            chatRoomService.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        redisPublisher.publish(chatRoomService.getTopic(message.getRoomId()), message);
    }

    private boolean isJoin(ChatMessage messageType) {
        return messageType.getMessageType().equals(ChatMessage.MessageType.JOIN);
    }
}
