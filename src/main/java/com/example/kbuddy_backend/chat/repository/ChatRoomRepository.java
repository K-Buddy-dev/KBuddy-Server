package com.example.kbuddy_backend.chat.repository;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        redisTemplate.delete(CHAT_ROOMS);
        for(int i = 0; i < 5; i++){
            ChatRoom chatRoom = ChatRoom.of("test_" + i);
            opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        }
    }

    public void save(ChatRoom chatRoom) {
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
    }

    public ChatRoom findById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    public List<ChatRoom> findAll() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }
}
