package com.example.kbuddy_backend.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class ChatParticipationRepository {

    private static final String ROOMS_BY_USER_PREFIX = "CHAT_ROOMS_BY_USER:";
    private static final String USERS_BY_ROOM_PREFIX = "USERS_BY_CHAT_ROOM:";

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    public void addUserToRoom(Long userId, String roomId) {
        setOps().add(ROOMS_BY_USER_PREFIX + userId, roomId);
        setOps().add(USERS_BY_ROOM_PREFIX + roomId, userId.toString());
    }

    public void removeUserFromRoom(Long userId, String roomId) {
        setOps().remove(ROOMS_BY_USER_PREFIX + userId, roomId);
        setOps().remove(USERS_BY_ROOM_PREFIX + roomId, userId.toString());
    }

    public Set<Object> findRoomsByUser(Long userId) {
        Set<Object> s = setOps().members(ROOMS_BY_USER_PREFIX + userId);
        return s != null ? s : Collections.emptySet();
    }

    public Set<Object> findUsersByRoom(String roomId) {
        Set<Object> s = setOps().members(USERS_BY_ROOM_PREFIX + roomId);
        return s != null ? s : Collections.emptySet();
    }

    private SetOperations<String, Object> setOps() {
        return redisTemplate.opsForSet();
    }
}