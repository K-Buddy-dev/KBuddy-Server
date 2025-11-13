package com.example.kbuddy_backend.announcement.exception;

public class AnnouncementNotFoundException extends RuntimeException {
  public AnnouncementNotFoundException(String message) {
    super(message);
  }
}
