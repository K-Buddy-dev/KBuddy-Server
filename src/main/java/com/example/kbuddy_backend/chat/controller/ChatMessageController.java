package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.dto.ChatMessage;
import com.example.kbuddy_backend.chat.service.ChatMessageService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("kbuddy/v1/chat")
@Tag(name = "Chat API", description = "채팅 메시지 API")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // REST 메시지 전송 (테스트/대체용)
    @PostMapping("/rooms/{roomId}/messages")
    public void sendMessageRest(@PathVariable String roomId,
                                @RequestBody ChatMessage message,
                                @CurrentUser User user) {
        message.setRoomId(roomId);
        if (message.getSender() == null) {
            message.setSender(String.valueOf(user.getId()));
        }
        chatMessageService.send(message);
    }

    // 최근 N개 조회
    @GetMapping("/rooms/{roomId}/messages/recent")
    public List<ChatMessage> getRecent(@PathVariable String roomId,
                                       @RequestParam(defaultValue = "50") int limit) {
        return chatMessageService.getRecent(roomId, limit);
    }

    // 페이지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessage> getHistory(@PathVariable String roomId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "50") int size) {
        return chatMessageService.getHistory(roomId, page, size);
    }

    // 전체 조회
    @GetMapping("/rooms/{roomId}/messages/all")
    public List<ChatMessage> getAll(@PathVariable String roomId) {
        return chatMessageService.getAll(roomId);
    }
}