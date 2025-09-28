package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.base.redis.service.RedisSubscriber;
import com.example.kbuddy_backend.chat.dto.ChatRoom;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final RedisSubscriber redisSubscriber;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final Map<String, ChannelTopic> topics = new HashMap<>();

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRoomRepository.findById(roomId);
    }

    public void createRoom(String name) {
        ChatRoom chatRoom = ChatRoom.of(name);
        chatRoomRepository.save(chatRoom);
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
}
