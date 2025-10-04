package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import com.example.kbuddy_backend.chat.repository.ChatParticipationRepository;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatParticipationService {

    private final ChatParticipationRepository participationRepository;
    private final ChatRoomRepository chatRoomRepository;

    public void join(Long userId, String roomId) {
        participationRepository.addUserToRoom(userId, roomId);
    }

    public void leave(Long userId, String roomId) {
        participationRepository.removeUserFromRoom(userId, roomId);
    }

    public List<ChatRoom> findRoomsByUser(Long userId) {
        Set<Object> roomIds = participationRepository.findRoomsByUser(userId);
        return roomIds.stream()
                .map(Object::toString)
                .map(chatRoomRepository::findById)
                .collect(Collectors.toList());
    }
}