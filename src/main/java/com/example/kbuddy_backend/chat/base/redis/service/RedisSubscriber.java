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
            String publishMessage = redisTemplate
                    .getStringSerializer()
                    .deserialize(message.getBody());

            ChatMessage roomMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            messageTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}