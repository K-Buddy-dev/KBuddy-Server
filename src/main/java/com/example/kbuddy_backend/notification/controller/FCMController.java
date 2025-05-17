package com.example.kbuddy_backend.notification.controller;

import com.example.kbuddy_backend.notification.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FCM", description = "FCM 알림 API 목록")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class FCMController {

    private final FCMService fcmService;

    @Operation(summary = "FCM 알림 전송", description = "특정 디바이스에 FCM 알림을 전송합니다.")
    @PostMapping("/send")
    public String sendNotification(
            @Parameter(description = "FCM 디바이스 토큰", required = true)
            @RequestParam String token,

            @Parameter(description = "알림 제목", required = true)
            @RequestParam String title,

            @Parameter(description = "알림 내용", required = true)
            @RequestParam String body
    ) {
        fcmService.sendNotification(token, title, body);
        return "알림이 성공적으로 전송되었습니다.";
    }
}