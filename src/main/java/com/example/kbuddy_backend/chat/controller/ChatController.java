package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.dto.ChatMessage;
import com.example.kbuddy_backend.chat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Controller
//@RequestMapping("kbuddy/v1/chat")
@Slf4j
@Tag(name = "Chat API", description = "채팅 API 목록")
public class ChatController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/message")
    public void sendMessage(@Payload ChatMessage message) {
        log.info("ChatController @MessageMapping hit: {}", message);
        chatMessageService.send(message);
    }

    private boolean isJoin(ChatMessage messageType) {
        return messageType.getMessageType().equals(ChatMessage.MessageType.JOIN);
    }
}
