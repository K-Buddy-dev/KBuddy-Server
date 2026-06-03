package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.base.redis.service.RedisSubscriber;
import com.example.kbuddy_backend.chat.entity.ChatRoom;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final RedisSubscriber redisSubscriber;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final Map<String, ChannelTopic> topics = new HashMap<>();

    public List<com.example.kbuddy_backend.chat.dto.ChatRoom> findAll() {
        return chatRoomRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public com.example.kbuddy_backend.chat.dto.ChatRoom findRoomById(String roomId) {
        return chatRoomRepository.findByRoomId(roomId)
                .map(this::toDto)
                .orElse(null);
    }

    public void createRoom(String name, Long counselorId, Long clientId) {
        createRoom(name, counselorId, clientId, null);
    }

    public String createRoom(String name, Long counselorId, Long clientId, Long bookingId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(UUID.randomUUID().toString());
        chatRoom.setName(name);
        chatRoom.setCounselorId(counselorId);
        chatRoom.setClientId(clientId);
        chatRoom.setBookingId(bookingId);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    public com.example.kbuddy_backend.chat.dto.ChatRoom findRoomByBookingId(Long bookingId) {
        return chatRoomRepository.findByBookingId(bookingId)
                .map(this::toDto)
                .orElse(null);
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListenerContainer.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }

    public void exitChatRoom(String roomId) {
        ChannelTopic topic = topics.remove(roomId);
        if (topic != null) {
            redisMessageListenerContainer.removeMessageListener(redisSubscriber, topic);
        }
    }

    public ChannelTopic getOrCreateTopic(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListenerContainer.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
        return topic;
    }

    private com.example.kbuddy_backend.chat.dto.ChatRoom toDto(ChatRoom room) {
        return com.example.kbuddy_backend.chat.dto.ChatRoom.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .build();
    }
}
