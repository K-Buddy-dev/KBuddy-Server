package com.example.kbuddy_backend.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.kbuddy_backend.chat.base.redis.service.RedisSubscriber;
import com.example.kbuddy_backend.chat.entity.ChatRoom;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private RedisSubscriber redisSubscriber;

    @Mock
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    void findOrCreateRoomReturnsExistingRoom() {
        ChatRoom existing = new ChatRoom();
        existing.setRoomId("existing-room");
        existing.setBookingId(10L);
        given(chatRoomRepository.findByBookingId(10L)).willReturn(Optional.of(existing));

        String roomId = chatRoomService.findOrCreateRoom("name", 2L, 1L, 10L);

        assertEquals("existing-room", roomId);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void findOrCreateRoomCreatesRoomWhenMissing() {
        given(chatRoomRepository.findByBookingId(10L)).willReturn(Optional.empty());

        String roomId = chatRoomService.findOrCreateRoom("name", 2L, 1L, 10L);

        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(captor.capture());
        ChatRoom saved = captor.getValue();
        assertNotNull(roomId);
        assertEquals(roomId, saved.getRoomId());
        assertEquals(10L, saved.getBookingId());
    }
}
