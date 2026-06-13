package com.example.kbuddy_backend.notification.entity;

/**
 * 알림의 종류를 정의하는 enum
 * 각 알림 타입은 특정 이벤트에 대한 알림을 나타냄
 */
public enum NotificationType {
    FREE_NOTIFICATION,
    QNA_LIKE_NOTIFICATION,
    QNA_COMMENT_NOTIFICATION,
    QNA_COMMENT_LIKE_NOTIFICATION,
    BLOG_LIKE_NOTIFICATION,
    BLOG_COMMENT_NOTIFICATION,
    BLOG_COMMENT_LIKE_NOTIFICATION,
    CHAT_MESSAGE_NOTIFICATION,       // 채팅 메시지 수신
    BOOKING_REQUEST_NOTIFICATION,    // 새 예약 요청 (상담사 수신)
    BOOKING_CONFIRMED_NOTIFICATION,  // 예약 확정
    BOOKING_CANCELLED_NOTIFICATION,  // 예약 취소
    INQUIRY_NOTIFICATION,            // 새 문의 (상담사 수신)
    INQUIRY_REPLY_NOTIFICATION       // 문의 답변 (고객 수신)
} 