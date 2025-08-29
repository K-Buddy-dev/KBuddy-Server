//package com.example.kbuddy_backend.chat.repository;
//
//import com.example.kbuddy_backend.chat.dto.ChatRoom;
//import com.example.kbuddy_backend.common.IntegrationTest;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class ChatRoomRepositoryTest extends IntegrationTest {
//
//    @Autowired
//    private ChatRoomRepository repository;
//
//    @Test
//    @DisplayName("채팅방 저장/조회")
//    void saveAndFind() {
//        ChatRoom room = ChatRoom.of("r1");
//        repository.save(room);
//
//        ChatRoom found = repository.findById(room.getRoomId());
//        assertThat(found.getRoomId()).isEqualTo(room.getRoomId());
//
//        List<ChatRoom> all = repository.findAll();
//        assertThat(all).isNotEmpty();
//    }
//}