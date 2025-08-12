package com.example.kbuddy_backend.user.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.dto.request.UserBlockRequest;
import com.example.kbuddy_backend.user.dto.response.UserBlockResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.service.UserBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/user/blocks")
@Tag(name = "User Block API", description = "사용자 차단 API 목록")
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping()
    @Operation(summary = "사용자 차단", description = "특정 사용자를 차단합니다.")
    public ResponseEntity<UserBlockResponse> blockUser(
            @RequestBody @Valid UserBlockRequest userBlockRequest,
            @Parameter(hidden = true) @CurrentUser User user) {
        UserBlockResponse response = userBlockService.blockUser(user, userBlockRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    @Operation(summary = "사용자 차단 해제", description = "차단한 사용자의 차단을 해제합니다.")
    public ResponseEntity<Void> unblockUser(
            @RequestBody @Valid UserBlockRequest userBlockRequest,
            @Parameter(hidden = true) @CurrentUser User user) {
        userBlockService.unblockUser(user, userBlockRequest.blockedUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    @Operation(summary = "차단한 사용자 목록 조회", description = "현재 사용자가 차단한 사용자 목록을 조회합니다.")
    public ResponseEntity<List<UserBlockResponse>> getBlockedUsers(
            @Parameter(hidden = true) @CurrentUser User user) {
        List<UserBlockResponse> blockedUsers = userBlockService.getBlockedUsers(user);
        return ResponseEntity.ok(blockedUsers);
    }

    @GetMapping("/blocked-by")
    @Operation(summary = "차단당한 사용자 목록 조회", description = "현재 사용자를 차단한 사용자 목록을 조회합니다.")
    public ResponseEntity<List<UserBlockResponse>> getBlockedByUsers(
            @Parameter(hidden = true) @CurrentUser User user) {
        List<UserBlockResponse> blockedByUsers = userBlockService.getBlockedByUsers(user);
        return ResponseEntity.ok(blockedByUsers);
    }
} 