package com.example.kbuddy_backend.announcement.exception;

import com.example.kbuddy_backend.common.exception.NotFoundException;

public class AnnouncementImageNotFoundException extends NotFoundException {
    public AnnouncementImageNotFoundException() {
        super("공지사항을 찾을 수 없습니다.");
    }
}
