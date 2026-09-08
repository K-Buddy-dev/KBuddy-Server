package com.example.kbuddy_backend.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.kbuddy_backend.notification.entity.Notification;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.repository.NotificationRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FCMService fcmService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User receiver;

    @BeforeEach
    void setUp() {
        receiver = User.builder().email("user@kbuddy.com").username("user").build();
    }

    @Test
    void notifyOnceSkipsExistingEvent() {
        given(notificationRepository.existsByEventKey("event-1")).willReturn(true);

        notificationService.notifyOnce(
                "event-1",
                receiver,
                "title",
                "body",
                NotificationType.CHAT_MESSAGE_NOTIFICATION,
                "room-1");

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(fcmService, never())
                .sendNotificationAllFcmTokens(any(), any(), any(), any(), any());
    }

    @Test
    void notifyOnceSavesAndSendsNewEvent() {
        given(notificationRepository.existsByEventKey("event-1")).willReturn(false);

        notificationService.notifyOnce(
                "event-1",
                receiver,
                "title",
                "body",
                NotificationType.CHAT_MESSAGE_NOTIFICATION,
                "room-1");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("event-1", captor.getValue().getEventKey());
        verify(fcmService).sendNotificationAllFcmTokens(
                receiver,
                "title",
                "body",
                NotificationType.CHAT_MESSAGE_NOTIFICATION.name(),
                "room-1");
    }
}
