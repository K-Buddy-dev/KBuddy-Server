package com.example.kbuddy_backend.announcement.exception;

public class AnnouncementNotFoundException extends RuntimeException {
    public AnnouncementNotFoundException() {
        super("공지사항 게시글을 찾을 수 없습니다.");
    }
}
