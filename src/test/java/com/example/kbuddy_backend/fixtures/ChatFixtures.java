//package com.example.kbuddy_backend.fixtures;
//
//import com.example.kbuddy_backend.chat.dto.ChatMessage;
//import com.example.kbuddy_backend.chat.dto.ChatRoom;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChatFixtures {
//
//    public static final String DEFAULT_ROOM_ID = "room-1";
//    public static final String DEFAULT_ROOM_NAME = "test-room";
//    public static final String DEFAULT_SENDER = "user-1";
//    public static final String DEFAULT_ROLE = "CLIENT";
//
//    // ChatRoom 생성
//    public static ChatRoom createRoom() {
//        return ChatRoom.builder()
//                .roomId(DEFAULT_ROOM_ID)
//                .name(DEFAULT_ROOM_NAME)
//                .build();
//    }
//
//    public static ChatRoom createRoom(String roomId, String name) {
//        return ChatRoom.builder()
//                .roomId(roomId)
//                .name(name)
//                .build();
//    }
//
//    // TALK 메시지
//    public static ChatMessage createTalk(String roomId, String sender, String text) {
//        return ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.TALK)
//                .roomId(roomId)
//                .sender(sender)
//                .message(text)
//                .role(DEFAULT_ROLE)
//                .build();
//    }
//
//    public static ChatMessage createTalk(String text) {
//        return createTalk(DEFAULT_ROOM_ID, DEFAULT_SENDER, text);
//    }
//
//    // JOIN 메시지
//    public static ChatMessage createJoin(String roomId, String sender) {
//        return ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.JOIN)
//                .roomId(roomId)
//                .sender(sender)
//                .message(sender + "님이 입장하셨습니다.")
//                .role(DEFAULT_ROLE)
//                .build();
//    }
//
//    public static ChatMessage createJoin() {
//        return createJoin(DEFAULT_ROOM_ID, DEFAULT_SENDER);
//    }
//
//    // LEAVE 메시지
//    public static ChatMessage createLeave(String roomId, String sender) {
//        return ChatMessage.builder()
//                .messageType(ChatMessage.MessageType.LEAVE)
//                .roomId(roomId)
//                .sender(sender)
//                .message(sender + "님이 퇴장하셨습니다.")
//                .role(DEFAULT_ROLE)
//                .build();
//    }
//
//    public static ChatMessage createLeave() {
//        return createLeave(DEFAULT_ROOM_ID, DEFAULT_SENDER);
//    }
//
//    // 연속 TALK 메시지
//    public static List<ChatMessage> createTalkList(String roomId, String sender, int count) {
//        List<ChatMessage> list = new ArrayList<>(count);
//        for (int i = 1; i <= count; i++) {
//            list.add(createTalk(roomId, sender, "msg-" + i));
//        }
//        return list;
//    }
//
//    public static List<ChatMessage> createTalkList(int count) {
//        return createTalkList(DEFAULT_ROOM_ID, DEFAULT_SENDER, count);
//    }
//
//    // JOIN → N TALK → LEAVE 시퀀스
//    public static List<ChatMessage> createJoinTalkLeave(String roomId, String sender, int talkCount) {
//        List<ChatMessage> list = new ArrayList<>(talkCount + 2);
//        list.add(createJoin(roomId, sender));
//        list.addAll(createTalkList(roomId, sender, talkCount));
//        list.add(createLeave(roomId, sender));
//        return list;
//    }
//
//    public static List<ChatMessage> createJoinTalkLeave(int talkCount) {
//        return createJoinTalkLeave(DEFAULT_ROOM_ID, DEFAULT_SENDER, talkCount);
//    }
//}