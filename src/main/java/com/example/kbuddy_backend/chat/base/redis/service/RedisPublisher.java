package com.example.kbuddy_backend.chat.base.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.kbuddy_backend.chat.dto.ChatMessage;

@RequiredArgsConstructor
@Service
public class RedisPublisher {
    @Qualifier("webSocketRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChannelTopic topic, ChatMessage message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
