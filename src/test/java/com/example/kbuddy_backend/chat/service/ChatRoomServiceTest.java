package com.example.kbuddy_backend.chat.service;

import com.example.kbuddy_backend.chat.dto.ChatRoom;
import com.example.kbuddy_backend.chat.repository.ChatRoomRepository;
import com.example.kbuddy_backend.chat.base.redis.service.RedisSubscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChatRoomServiceTest {

    @Test
    @DisplayName("채팅방 생성/조회/입장/퇴장 동작")
    void roomLifecycle() {
        ChatRoomRepository repo = mock(ChatRoomRepository.class);
        RedisSubscriber sub = mock(RedisSubscriber.class);
        RedisMessageListenerContainer container = mock(RedisMessageListenerContainer.class);

        ChatRoomService service = new ChatRoomService(repo, sub, container);

        // findAll
        when(repo.findAll()).thenReturn(List.of(ChatRoom.of("test")));
        assertThat(service.findAll()).hasSize(1);

        // createRoom
        service.createRoom("new-room");
        verify(repo, times(1)).save(Mockito.any(ChatRoom.class));

        // enter -> listener 등록
        service.enterChatRoom("room-1");
        verify(container, times(1)).addMessageListener(eq(sub), any(ChannelTopic.class));

        // getOrCreateTopic -> 재활용
        ChannelTopic t1 = service.getOrCreateTopic("room-1");
        ChannelTopic t2 = service.getOrCreateTopic("room-1");
        assertThat(t1.getTopic()).isEqualTo("room-1");
        assertThat(t2).isSameAs(t1);

        // exit -> listener 제거
        service.exitChatRoom("room-1");
        verify(container, times(1)).removeMessageListener(eq(sub), any(ChannelTopic.class));
    }
}