package com.example.kbuddy_backend.chat.repository;

import com.example.kbuddy_backend.chat.entity.ChatReadStatus;
import com.example.kbuddy_backend.chat.entity.ChatRoom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

    Optional<ChatReadStatus> findByUserIdAndRoom(Long userId, ChatRoom room);
}
