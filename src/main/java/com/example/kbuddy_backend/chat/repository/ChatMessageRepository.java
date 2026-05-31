package com.example.kbuddy_backend.chat.repository;

import com.example.kbuddy_backend.chat.entity.ChatMessage;
import com.example.kbuddy_backend.chat.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    ChatMessage findTop1ByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);
    List<ChatMessage> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);
    Page<ChatMessage> findByChatRoom(ChatRoom chatRoom, Pageable pageable);
    long countByChatRoom(ChatRoom chatRoom);
    long countByChatRoomAndSentAtAfter(ChatRoom chatRoom, LocalDateTime after);
}
