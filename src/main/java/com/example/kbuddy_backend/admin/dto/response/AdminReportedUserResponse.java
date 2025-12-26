package com.example.kbuddy_backend.admin.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportedUserResponse {

    private final Long userId;
    private final String username;
    private final String email;
    private final long totalReports;
    private final List<AdminReportedPostResponse> posts;
}


