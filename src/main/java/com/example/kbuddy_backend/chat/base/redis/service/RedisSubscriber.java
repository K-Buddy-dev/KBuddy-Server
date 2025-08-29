package com.example.kbuddy_backend.chat.base.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import com.example.kbuddy_backend.chat.dto.ChatMessage;


@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;

    @Qualifier("webSocketRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Object payload = redisTemplate.getValueSerializer().deserialize(message.getBody());
            ChatMessage roomMessage;

            if (payload instanceof ChatMessage cm) {
                roomMessage = cm;
            } else if (payload instanceof String s) {
                roomMessage = objectMapper.readValue(s, ChatMessage.class);
            } else {
                // GenericJackson2JsonRedisSerializer가 Map 등으로 반환하는 경우
                roomMessage = objectMapper.convertValue(payload, ChatMessage.class);
            }

            messageTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}