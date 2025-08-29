//package com.example.kbuddy_backend.chat.controller;
//
//import com.example.kbuddy_backend.chat.dto.ChatMessage;
//import com.example.kbuddy_backend.chat.service.ChatMessageService;
//import com.example.kbuddy_backend.common.WebMVCTest;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//
//import java.util.List;
//import java.util.Optional;
//import com.example.kbuddy_backend.user.entity.User;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WithMockUser(username = "123", roles = "USER")
//class ChatMessageControllerTest extends WebMVCTest {
//
//    @MockBean
//    private ChatMessageService chatMessageService;
//
//    @DisplayName("REST 메시지 전송 API 호출시 서비스로 위임된다")
//    @Test
//    void sendMessageRest() throws Exception {
//        // @CurrentUser 리졸버가 호출하는 UserRepository 스텁
//        when(userRepository.findById(123L)).thenReturn(Optional.of(User.builder().username("mock").build()));
//
//        ChatMessage req = ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.TALK)
//                .sender("123")
//                .message("hi")
//                .build();
//
//        mockMvc.perform(post("/kbuddy/v1/chat/rooms/{roomId}/messages", "room-1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isOk());
//
//        verify(chatMessageService, times(1)).send(any(ChatMessage.class));
//    }
//
//    @DisplayName("메시지 최근 N개 조회")
//    @Test
//    void getRecent() throws Exception {
//        when(chatMessageService.getRecent("room-1", 50)).thenReturn(List.of());
//
//        mockMvc.perform(get("/kbuddy/v1/chat/rooms/{roomId}/messages/recent?limit=50", "room-1"))
//                .andExpect(status().isOk());
//
//        verify(chatMessageService, times(1)).getRecent("room-1", 50);
//    }
//
//    @DisplayName("메시지 페이지 조회")
//    @Test
//    void getHistory() throws Exception {
//        when(chatMessageService.getHistory("room-1", 0, 20)).thenReturn(List.of());
//
//        mockMvc.perform(get("/kbuddy/v1/chat/rooms/{roomId}/messages?page=0&size=20", "room-1"))
//                .andExpect(status().isOk());
//
//        verify(chatMessageService, times(1)).getHistory("room-1", 0, 20);
//    }
//
//    @DisplayName("메시지 전체 조회")
//    @Test
//    void getAll() throws Exception {
//        when(chatMessageService.getAll("room-1")).thenReturn(List.of());
//
//        mockMvc.perform(get("/kbuddy/v1/chat/rooms/{roomId}/messages/all", "room-1"))
//                .andExpect(status().isOk());
//
//        verify(chatMessageService, times(1)).getAll("room-1");
//    }
//}