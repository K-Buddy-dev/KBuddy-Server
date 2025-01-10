package com.example.kbuddy_backend.qna.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record QnaSaveRequest(String title, String description, MultipartFile image, List<String> hashtags) {
    public static QnaSaveRequest of(String title, String description, MultipartFile image, List<String> hashtags) {
        return new QnaSaveRequest(title, description, image, hashtags);
    }
}
