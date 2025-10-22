package com.example.kbuddy_backend.notification.service;

import com.example.kbuddy_backend.notification.entity.FCMToken;
import com.example.kbuddy_backend.user.entity.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.AndroidConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
     * @param deviceInfo 디바이스 타입(android/ios) 등
     */
    public void sendNotification(String token, String title, String body, String type, String targetId, String deviceInfo) {
        try {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("click_action", type)
                    .putData("deep_link", targetId)
                    .putData("time", String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

            // iOS의 경우 Notification 포함, Android의 경우 미포함
            if (!"android".equalsIgnoreCase(deviceInfo)) {
                builder.setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
            }

            // iOS 설정
            if ("ios".equalsIgnoreCase(deviceInfo)) {
                builder.setApnsConfig(ApnsConfig.builder()
                        .putHeader("apns-priority", "10")
                        .setAps(Aps.builder()
                                .setContentAvailable(true)
                                .setSound("default")
                                .build())
                        .build());
            }

            // Android 설정
            if ("android".equalsIgnoreCase(deviceInfo)) {
                builder.setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build());
            }

            Message message = builder.build();

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
    public void sendNotificationAllFcmTokens(User user, String title, String message, String type, String targetId) {
        // 사용자의 모든 활성 FCM 토큰 조회
        List<FCMToken> activeTokens = fcmTokenService.getActiveTokens(user);

        // 각 토큰에 대해 알림 전송
        for (FCMToken token : activeTokens) {
            try {
                sendNotification(token.getToken(), title, message, type, targetId, token.getDeviceInfo());
            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 비활성화
                fcmTokenService.deactivateToken(token.getToken());
            }
        }
    }
}