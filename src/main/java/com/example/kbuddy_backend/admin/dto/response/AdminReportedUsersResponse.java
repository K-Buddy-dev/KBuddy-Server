package com.example.kbuddy_backend.admin.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportedUsersResponse {

    private final int totalUsers;
    private final List<AdminReportedUserResponse> users;
}


