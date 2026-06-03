package com.example.kbuddy_backend.admin.controller;

import com.example.kbuddy_backend.admin.dto.request.AdminNotificationRequest;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedPostsResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedUsersResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberListResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberStatsResponse;
import com.example.kbuddy_backend.admin.service.AdminDashboardService;
import com.example.kbuddy_backend.admin.service.AdminAuthService;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import com.example.kbuddy_backend.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AdminAuthService adminAuthService;
    private final NotificationService notificationService;

    @GetMapping("/subscribers/stats")
    public ResponseEntity<AdminSubscriberStatsResponse> getSubscriberStats() {
        return ResponseEntity.ok(adminDashboardService.getSubscriberStats());
    }

    @GetMapping("/subscribers/list")
    public ResponseEntity<AdminSubscriberListResponse> getSubscribers(
            @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminDashboardService.getSubscribers(pageable));
    }

    @GetMapping("/reports/users")
    public ResponseEntity<AdminReportedUsersResponse> getReportedUsers() {
        return ResponseEntity.ok(adminDashboardService.getReportedUsers());
    }

    @GetMapping("/reports/posts")
    public ResponseEntity<AdminReportedPostsResponse> getReportedPosts() {
        return ResponseEntity.ok(adminDashboardService.getReportedPosts());
    }

    @PostMapping("/notifications")
    public ResponseEntity<Map<String, Integer>> sendNotificationToAll(
            @Valid @RequestBody AdminNotificationRequest request) {
        int count = notificationService.notifyAll(request.title(), request.message(), request.targetId());
        return ResponseEntity.ok(Map.of("sentCount", count));
    }
}
