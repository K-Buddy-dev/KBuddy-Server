package com.example.kbuddy_backend.notification.service;

import com.example.kbuddy_backend.notification.entity.Notification;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.repository.NotificationRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    /**
     * 새로운 알림 생성 및 FCM 푸시 알림 전송
     */
    @Transactional
    public void notify(User receiver, String title, String body, NotificationType type, String targetId) {
        Notification notification = Notification.createNotification(receiver, title, body, type, targetId);
        notificationRepository.save(notification);
        fcmService.sendNotificationAllFcmTokens(receiver, title, body, type.name(), targetId);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findByIdAndReceiver(notificationId, user)
                .ifPresent(Notification::markAsRead);
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

    @Transactional
    public int notifyAll(String title, String message, String targetId) {
        List<User> users = userRepository.findAll();
        users.forEach(user -> notify(user, title, message, NotificationType.FREE_NOTIFICATION, targetId));
        return users.size();
    }
} 