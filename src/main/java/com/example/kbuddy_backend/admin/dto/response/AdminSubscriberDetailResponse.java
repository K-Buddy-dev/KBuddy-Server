package com.example.kbuddy_backend.admin.dto.response;

import com.example.kbuddy_backend.user.constant.Gender;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubscriberDetailResponse {

    private final Long userId;
    private final String username;
    private final String email;
    private final Gender gender;
    private final boolean active;
    private final LocalDateTime createdAt;
}


