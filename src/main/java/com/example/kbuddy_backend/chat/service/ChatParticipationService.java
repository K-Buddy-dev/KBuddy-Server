package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.dto.ChatRoomSummary;
import com.example.kbuddy_backend.chat.entity.ChatMessage;
import com.example.kbuddy_backend.chat.entity.ChatReadStatus;
import com.example.kbuddy_backend.chat.entity.ChatRoom;
import com.example.kbuddy_backend.chat.repository.ChatMessageRepository;
import com.example.kbuddy_backend.chat.repository.ChatReadStatusRepository;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatParticipationService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final UserRepository userRepository;

    public void join(Long userId, String roomId) {
        chatRoomRepository.findByRoomId(roomId);
    }

    public void leave(Long userId, String roomId) {
        chatRoomRepository.findByRoomId(roomId);
    }

    @Transactional
    public void markAsRead(Long userId, String roomId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        chatReadStatusRepository.findByUserIdAndRoom(userId, room)
                .ifPresentOrElse(
                        status -> status.updateLastReadAt(LocalDateTime.now(java.time.ZoneOffset.UTC)),
                        () -> chatReadStatusRepository.save(
                                ChatReadStatus.builder()
                                        .userId(userId)
                                        .room(room)
                                        .lastReadAt(LocalDateTime.now(java.time.ZoneOffset.UTC))
                                        .build()));
    }

    public List<com.example.kbuddy_backend.chat.dto.ChatRoom> findRoomsByUser(Long userId) {
        return chatRoomRepository.findByCounselorIdOrClientId(userId, userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ChatRoomSummary findRoomSummaryById(String roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
        return toSummary(room, userId);
    }

    public List<ChatRoomSummary> findRoomSummariesByUser(Long userId) {
        return chatRoomRepository.findByCounselorIdOrClientId(userId, userId).stream()
                .map(room -> toSummary(room, userId))
                .sorted(Comparator.comparing(ChatRoomSummary::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private ChatRoomSummary toSummary(ChatRoom room, Long userId) {
        Long peerId = resolvePeerId(room, userId);
        User peer = peerId != null ? userRepository.findById(peerId).orElse(null) : null;
        ChatMessage lastMessage = chatMessageRepository.findTop1ByChatRoomOrderBySentAtDesc(room);

        LocalDateTime lastReadAt = chatReadStatusRepository.findByUserIdAndRoom(userId, room)
                .map(ChatReadStatus::getLastReadAt)
                .orElse(null);
        long unreadCount = lastReadAt != null
                ? chatMessageRepository.countByChatRoomAndSentAtAfter(room, lastReadAt)
                : chatMessageRepository.countByChatRoom(room);

        return ChatRoomSummary.builder()
                .roomId(room.getRoomId())
                .roomName(room.getName())
                .peerUserId(peerId)
                .peerNickname(peer != null ? peer.getUsername() : null)
                .peerProfileImageUrl(peer != null ? peer.getProfileImageUrl() : null)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getSentAt() : null)
                .unreadCount(unreadCount)
                .build();
    }

    private Long resolvePeerId(ChatRoom room, Long userId) {
        return userId.equals(room.getCounselorId()) ? room.getClientId() : room.getCounselorId();
    }

    private com.example.kbuddy_backend.chat.dto.ChatRoom toDto(ChatRoom room) {
        return com.example.kbuddy_backend.chat.dto.ChatRoom.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .build();
    }
}
