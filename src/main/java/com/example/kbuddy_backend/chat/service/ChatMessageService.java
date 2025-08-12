// src/main/java/com/example/kbuddy_backend/chat/service/ChatMessageService.java
package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.base.redis.service.RedisPublisher;
import com.example.kbuddy_backend.chat.dto.ChatMessage;
import com.example.kbuddy_backend.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisPublisher redisPublisher;
    private final ChatRoomService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;

    public void send(ChatMessage message) {
        String roomId = message.getRoomId();

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

        ChannelTopic topic = chatRoomService.getOrCreateTopic(roomId);
        redisPublisher.publish(topic, message);
        // 저장은 Subscriber에서 단일화 처리(권장). 직접 저장하려면 아래 주석 해제
        // chatMessageRepository.save(message);
    }

    public List<ChatMessage> getRecent(String roomId, int limit) {
        return chatMessageRepository.findRecentByRoom(roomId, limit);
    }

    public List<ChatMessage> getHistory(String roomId, int page, int size) {
        return chatMessageRepository.findByRoomPaged(roomId, page, size);
    }

    public List<ChatMessage> getAll(String roomId) {
        return chatMessageRepository.findAllByRoom(roomId);
    }
}