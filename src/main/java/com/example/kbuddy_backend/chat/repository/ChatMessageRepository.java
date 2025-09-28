package com.example.kbuddy_backend.chat.repository;

import com.example.kbuddy_backend.chat.dto.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class ChatMessageRepository {

    private static final String CHAT_MESSAGE_KEY_PREFIX = "CHAT_MESSAGE:";

    // 값 직렬화가 String인 템플릿 사용
    @Qualifier("redisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private String key(String roomId) {
        return CHAT_MESSAGE_KEY_PREFIX + roomId;
    }

    public void save(ChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            listOps().rightPush(key(message.getRoomId()), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ChatMessage", e);
        }
    }

    public List<ChatMessage> findAllByRoom(String roomId) {
        List<Object> raw = listOps().range(key(roomId), 0, -1);
        return toMessages(raw);
    }

    public List<ChatMessage> findRecentByRoom(String roomId, int limit) {
        Long size = listOps().size(key(roomId));
        if (size == null || size == 0) return Collections.emptyList();
        long start = Math.max(0, size - limit);
        long end = size - 1;
        List<Object> raw = listOps().range(key(roomId), start, end);
        return toMessages(raw);
    }

    public List<ChatMessage> findByRoomPaged(String roomId, int page, int size) {
        long start = (long) page * size;
        long end = start + size - 1;
        List<Object> raw = listOps().range(key(roomId), start, end);
        return toMessages(raw);
    }

    private ListOperations<String, Object> listOps() {
        return redisTemplate.opsForList();
    }

    private List<ChatMessage> toMessages(List<Object> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(o -> {
                    try {
                        return objectMapper.readValue(o.toString(), ChatMessage.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize ChatMessage", e);
                    }
                })
                .collect(Collectors.toList());
    }
}