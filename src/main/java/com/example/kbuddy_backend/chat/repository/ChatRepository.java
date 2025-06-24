package com.example.kbuddy_backend.chat.repository;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class ChatRepository {
    private final Map<String, ChatRoom> chatRooms;

    public void save(String roomId, ChatRoom chatRoom) {
        chatRooms.put(roomId, chatRoom);
    }

    public ChatRoom findById(String roomId) {
        return chatRooms.get(roomId);
    }

    public List<ChatRoom> findAll() {
        return new ArrayList<>(chatRooms.values());
    }
}
