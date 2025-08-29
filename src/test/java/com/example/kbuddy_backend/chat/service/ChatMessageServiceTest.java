//package com.example.kbuddy_backend.chat.service;
//
//import com.example.kbuddy_backend.chat.base.redis.service.RedisPublisher;
//import com.example.kbuddy_backend.chat.dto.ChatMessage;
//import com.example.kbuddy_backend.chat.repository.ChatMessageRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.redis.listener.ChannelTopic;
//
//import static org.mockito.Mockito.*;
//
//class ChatMessageServiceTest {
//
//    @Test
//    @DisplayName("JOIN은 입장 메시지로 변환 후 publish")
//    void sendJoin() {
//        RedisPublisher publisher = mock(RedisPublisher.class);
//        ChatRoomService roomService = mock(ChatRoomService.class);
//        ChatMessageRepository repo = mock(ChatMessageRepository.class);
//        when(roomService.getOrCreateTopic("room-1")).thenReturn(new ChannelTopic("room-1"));
//
//        ChatMessageService svc = new ChatMessageService(publisher, roomService, repo);
//
//        ChatMessage msg = ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.JOIN)
//                .roomId("room-1")
//                .sender("u1")
//                .build();
//
//        svc.send(msg);
//
//        verify(roomService, times(1)).enterChatRoom("room-1");
//        verify(publisher, times(1)).publish(any(ChannelTopic.class), eq(msg));
//    }
//
//    @Test
//    @DisplayName("LEAVE는 publish 후 exit 호출")
//    void sendLeave() {
//        RedisPublisher publisher = mock(RedisPublisher.class);
//        ChatRoomService roomService = mock(ChatRoomService.class);
//        ChatMessageRepository repo = mock(ChatMessageRepository.class);
//        when(roomService.getOrCreateTopic("room-1")).thenReturn(new ChannelTopic("room-1"));
//
//        ChatMessageService svc = new ChatMessageService(publisher, roomService, repo);
//
//        ChatMessage msg = ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.LEAVE)
//                .roomId("room-1")
//                .sender("u1")
//                .build();
//
//        svc.send(msg);
//
//        verify(publisher, times(1)).publish(any(ChannelTopic.class), eq(msg));
//        verify(roomService, times(1)).exitChatRoom("room-1");
//    }
//
//    @Test
//    @DisplayName("TALK는 토픽 보장 후 publish")
//    void sendTalk() {
//        RedisPublisher publisher = mock(RedisPublisher.class);
//        ChatRoomService roomService = mock(ChatRoomService.class);
//        ChatMessageRepository repo = mock(ChatMessageRepository.class);
//        when(roomService.getOrCreateTopic("room-1")).thenReturn(new ChannelTopic("room-1"));
//
//        ChatMessageService svc = new ChatMessageService(publisher, roomService, repo);
//
//        ChatMessage msg = ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.TALK)
//                .roomId("room-1")
//                .sender("u1")
//                .message("hi")
//                .build();
//
//        svc.send(msg);
//
//        verify(roomService, times(1)).enterChatRoom("room-1");
//        verify(publisher, times(1)).publish(any(ChannelTopic.class), eq(msg));
//    }
//}