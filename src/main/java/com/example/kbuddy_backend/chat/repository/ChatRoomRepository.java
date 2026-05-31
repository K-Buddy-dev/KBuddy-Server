package com.example.kbuddy_backend.chat.repository;

import com.example.kbuddy_backend.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);
    Optional<ChatRoom> findByBookingId(Long bookingId);
    List<ChatRoom> findByCounselorIdOrClientId(Long counselorId, Long clientId);
}
