package com.example.kbuddy_backend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FCMService {

    /**
     * FCM을 통해 알림을 전송하는 메서드
     * @param token 클라이언트의 FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     */
    public void sendNotification(String token, String title, String body) {
        try {
            // 알림 메시지 생성
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .build();

            // FCM을 통해 메시지 전송
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }
}
