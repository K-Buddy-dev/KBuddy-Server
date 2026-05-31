package com.example.kbuddy_backend.notification.service;

import com.example.kbuddy_backend.notification.entity.Notification;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.repository.NotificationRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    /**
     * 새로운 알림 생성 및 FCM 푸시 알림 전송
     */
    @Transactional
    public Notification createNotification(User receiver, String message, NotificationType type) {
        Notification notification = Notification.createNotification(receiver, message, type);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void notify(User receiver, String title, String body, NotificationType type, String targetId) {
        Notification notification = Notification.createNotification(receiver, body, type);
        notificationRepository.save(notification);
        fcmService.sendNotificationAllFcmTokens(receiver, title, body, type.name(), targetId);
    }

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }

    /**
     * 사용자의 모든 알림을 읽음 상태로 변경
     */
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(user);
    }
} 