package com.example.kbuddy_backend.admin.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubscriberListResponse {

    private final long totalElements;
    private final int page;
    private final int size;
    private final List<AdminSubscriberDetailResponse> subscribers;
}


