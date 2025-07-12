package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import com.example.kbuddy_backend.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("kbuddy/v1/chat")
@Tag(name = "Chat API", description = "채팅 API 목록")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @GetMapping("/rooms-all")
    public String getAllRooms() {
        return "/chat/home";
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> getRooms() {
        return chatRoomService.findAll();
    }

    @PostMapping("/room")
    public ResponseEntity<Void> createRoom(@RequestParam String name) {
        chatRoomService.createRoom(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom getRoom(@PathVariable String roomId) {
        return chatRoomService.findRoomById(roomId);
    }
}
