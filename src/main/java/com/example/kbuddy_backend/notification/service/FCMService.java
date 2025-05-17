package com.example.kbuddy_backend.notification.service;

import com.example.kbuddy_backend.notification.entity.FCMToken;
import com.example.kbuddy_backend.user.entity.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FCMService {

    private final FCMTokenService fcmTokenService;

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

    /**
     * 사용자에게 알림을 전송하는 메서드
     * 사용자의 모든 활성 FCM 토큰에 알림을 전송
     * @param user 알림을 받을 사용자
     * @param message 알림 메시지
     */
    public void sendNotification(User user, String message) {
        // 사용자의 모든 활성 FCM 토큰 조회
        List<FCMToken> activeTokens = fcmTokenService.getActiveTokens(user);
        
        // 각 토큰에 대해 알림 전송
        for (FCMToken token : activeTokens) {
            try {
                sendNotification(token.getToken(), "KBuddy 알림", message);
            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 비활성화
                fcmTokenService.deactivateToken(token.getToken());
            }
        }
    }
}