package com.example.kbuddy_backend.user.dto.response;

import java.time.LocalDateTime;

public record UserBlockResponse(
        Long id,
        Long blockerId,
        String blockerUsername,
        Long blockedId,
        String blockedUsername,
        LocalDateTime blockedAt
) {
    public static UserBlockResponse of(
            Long id,
            Long blockerId,
            String blockerUsername,
            Long blockedId,
            String blockedUsername,
            LocalDateTime blockedAt
    ) {
        return new UserBlockResponse(
                id,
                blockerId,
                blockerUsername,
                blockedId,
                blockedUsername,
                blockedAt
        );
    }
} 