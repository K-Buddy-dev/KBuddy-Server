package com.example.kbuddy_backend.admin.dto.response;

import com.example.kbuddy_backend.admin.dto.AdminPostType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportedPostResponse {

    private final AdminPostType postType;
    private final Long postId;
    private final String title;
    private final Long writerId;
    private final String writerUsername;
    private final String writerEmail;
    private final long reportCount;
}


