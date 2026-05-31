// src/main/java/com/example/kbuddy_backend/chat/service/ChatMessageService.java
package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.base.redis.service.RedisPublisher;
import com.example.kbuddy_backend.chat.dto.ChatMessage;
import com.example.kbuddy_backend.chat.entity.ChatRoom;
import com.example.kbuddy_backend.chat.constant.ChatRole;
import com.example.kbuddy_backend.chat.repository.ChatMessageRepository;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.service.NotificationService;
import com.example.kbuddy_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final RedisPublisher redisPublisher;
    private final ChatRoomService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void send(ChatMessage message) {
        String roomId = message.getRoomId();
        message.setSentAt(LocalDateTime.now(java.time.ZoneOffset.UTC));

        // 입장/퇴장 처리
        switch (message.getMessageType()) {
            case JOIN -> {
                chatRoomService.enterChatRoom(roomId);
                message.setMessage(message.getSender() + "님이 입장하셨습니다.");
            }
            case LEAVE -> {
                message.setMessage(message.getSender() + "님이 퇴장하셨습니다.");
                // 퇴장 메시지 발행 후 리스너 해제
                ChannelTopic topic = chatRoomService.getOrCreateTopic(roomId);
                redisPublisher.publish(topic, message);
                chatRoomService.exitChatRoom(roomId);
                return;
            }
            default -> {
                // TALK 등은 토픽만 보장
                chatRoomService.enterChatRoom(roomId);
            }
        }

        log.info("ChatMessageService: in -> {}", message);
        ChannelTopic topic = chatRoomService.getOrCreateTopic(roomId);
        chatRoomRepository.findByRoomId(roomId).ifPresent(room -> {
            Long senderId = parseSenderId(message.getSender());
            message.setRole(resolveRole(senderId, room).name());
            chatMessageRepository.save(toEntity(message, room));
            message.setSender(resolveUuid(senderId));

            // 상대방에게 새 메시지 알림
            if (ChatMessage.MessageType.TALK.equals(message.getMessageType()) && senderId != null) {
                Long receiverId = senderId.equals(room.getCounselorId()) ? room.getClientId() : room.getCounselorId();
                userRepository.findById(receiverId).ifPresent(receiver -> {
                    String senderName = userRepository.findById(senderId)
                            .map(u -> u.getUsername()).orElse("Someone");
                    notificationService.notify(receiver, "New Message", senderName + ": " + message.getMessage(),
                            NotificationType.CHAT_MESSAGE_NOTIFICATION, roomId);
                });
            }
        });
        log.info("ChatMessageService: publish -> {}", topic.getTopic());
        redisPublisher.publish(topic, message);
    }

    public List<ChatMessage> getRecent(String roomId, int limit) {
        return chatRoomRepository.findByRoomId(roomId)
                .map(room -> {
                    List<com.example.kbuddy_backend.chat.entity.ChatMessage> raw =
                            chatMessageRepository.findByChatRoom(room,
                                    PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sentAt")))
                                    .getContent();
                    List<ChatMessage> mapped = raw.stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
                    Collections.reverse(mapped);
                    return mapped;
                })
                .orElse(Collections.emptyList());
    }

    public List<ChatMessage> getHistory(String roomId, int page, int size) {
        return chatRoomRepository.findByRoomId(roomId)
                .map(room -> chatMessageRepository.findByChatRoom(
                                room, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt")))
                        .getContent()
                        .stream()
                        .map(this::toDto)
                        .collect(java.util.stream.Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public List<ChatMessage> getAll(String roomId) {
        return chatRoomRepository.findByRoomId(roomId)
                .map(room -> chatMessageRepository.findByChatRoomOrderBySentAtAsc(room).stream()
                        .map(this::toDto)
                        .collect(java.util.stream.Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private com.example.kbuddy_backend.chat.entity.ChatMessage toEntity(ChatMessage message, ChatRoom room) {
        Long senderId = parseSenderId(message.getSender());
        com.example.kbuddy_backend.chat.entity.ChatMessage entity =
                new com.example.kbuddy_backend.chat.entity.ChatMessage();
        entity.setChatRoom(room);
        entity.setSender(senderId);
        entity.setMessage(message.getMessage());
        entity.setSentAt(message.getSentAt());
        entity.setRole(resolveRole(senderId, room));
        entity.setClientMessageId(message.getClientMessageId());
        return entity;
    }

    private ChatMessage toDto(com.example.kbuddy_backend.chat.entity.ChatMessage entity) {
        return ChatMessage.builder()
                .messageType(ChatMessage.MessageType.TALK)
                .roomId(entity.getChatRoom().getRoomId())
                .sender(resolveUuid(entity.getSender())) // Long ID → UUID
                .message(entity.getMessage())
                .role(entity.getRole() != null ? entity.getRole().name() : null)
                .sentAt(entity.getSentAt())
                .clientMessageId(entity.getClientMessageId())
                .build();
    }

    // 클라이언트가 보낸 role 값은 신뢰하지 않고 room membership 기준으로 서버가 결정
    private ChatRole resolveRole(Long senderId, ChatRoom room) {
        if (senderId != null && senderId.equals(room.getCounselorId())) {
            return ChatRole.COUNSELOR;
        }
        return ChatRole.CLIENT;
    }

    private String resolveUuid(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(u -> u.getUuid().toString())
                .orElse(null);
    }

    private Long parseSenderId(String sender) {
        if (sender == null || sender.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(sender);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
