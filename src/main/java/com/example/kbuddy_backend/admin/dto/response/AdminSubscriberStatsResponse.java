package com.example.kbuddy_backend.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubscriberStatsResponse {

    private final long totalSubscribers;
    private final long maleSubscribers;
    private final long femaleSubscribers;
}


