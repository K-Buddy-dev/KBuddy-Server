package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import com.example.kbuddy_backend.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @GetMapping("/chat/rooms-all")
    public String getAllRooms() {
        return "/chat/home";
    }

    @GetMapping("/chat/rooms")
    @ResponseBody
    public List<ChatRoom> getRooms() {
        return chatRoomService.findAll();
    }

    @PostMapping("/chat/room")
    public String createRoom(@RequestParam String name) {
        chatRoomService.createRoom(name);
        return "redirect:/chat/rooms";
    }

    @GetMapping("/chat/room/{roomId}")
    @ResponseBody
    public ChatRoom getRoom(@PathVariable String roomId) {
        return chatRoomService.findRoomById(roomId);
    }
}
