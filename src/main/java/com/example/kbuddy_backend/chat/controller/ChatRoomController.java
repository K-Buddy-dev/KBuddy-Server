package com.example.kbuddy_backend.chat.controller;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import com.example.kbuddy_backend.chat.dto.ChatRoomSummary;
import com.example.kbuddy_backend.chat.service.ChatParticipationService;
import com.example.kbuddy_backend.chat.service.ChatRoomService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("kbuddy/v1/chat")
@Tag(name = "Chat API", description = "채팅방 API")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatParticipationService chatParticipationService;

    @GetMapping("/rooms")
    public List<ChatRoom> getRooms() {
        return chatRoomService.findAll();
    }

    @PostMapping("/room")
    public ResponseEntity<Void> createRoom(@RequestParam String name,
                                           @RequestParam Long counselorId,
                                           @RequestParam Long clientId) {
        chatRoomService.createRoom(name, counselorId, clientId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ChatRoomSummary> getRoom(@PathVariable String roomId, @CurrentUser User user) {
        return ResponseEntity.ok(chatParticipationService.findRoomSummaryById(roomId, user.getId()));
    }

    // 방 입장(멤버십)
    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<Void> joinRoom(@PathVariable String roomId, @CurrentUser User user) {
        chatRoomService.enterChatRoom(roomId);
        chatParticipationService.join(user.getId(), roomId);
        return ResponseEntity.ok().build();
    }

    // 방 퇴장(멤버십)
    @PostMapping("/room/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomId, @CurrentUser User user) {
        chatParticipationService.leave(user.getId(), roomId);
        // 필요 시 리스너 해제 로직은 서비스에서
        return ResponseEntity.ok().build();
    }

    // 사용자별 채팅방 목록
    @GetMapping("/users/me/rooms")
    public List<ChatRoomSummary> getMyRooms(@CurrentUser User user) {
        return chatParticipationService.findRoomSummariesByUser(user.getId());
    }

    // 예약 ID로 채팅방 조회
    @GetMapping("/bookings/{bookingId}/room")
    public ResponseEntity<ChatRoom> getRoomByBooking(@PathVariable Long bookingId) {
        ChatRoom room = chatRoomService.findRoomByBookingId(bookingId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    // 채팅방 메시지 읽음 처리
    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String roomId, @CurrentUser User user) {
        chatParticipationService.markAsRead(user.getId(), roomId);
        return ResponseEntity.noContent().build();
    }
}
