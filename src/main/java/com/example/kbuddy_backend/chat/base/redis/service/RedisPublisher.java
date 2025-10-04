package com.example.kbuddy_backend.chat.base.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.kbuddy_backend.chat.dto.ChatMessage;

@Slf4j
//@RequiredArgsConstructor
@Service
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisher(@Qualifier("webSocketRedisTemplate")
                          RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public void publish(ChannelTopic topic, ChatMessage message) {
        log.info("RedisPublisher - ValueSerializer: {}",
                redisTemplate.getValueSerializer().getClass().getSimpleName());
        log.info("RedisPublisher - Publishing to topic: {}", topic.getTopic());
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
