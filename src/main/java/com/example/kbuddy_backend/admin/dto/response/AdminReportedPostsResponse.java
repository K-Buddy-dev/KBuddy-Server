package com.example.kbuddy_backend.admin.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportedPostsResponse {

    private final int totalPosts;
    private final List<AdminReportedPostResponse> posts;
}


