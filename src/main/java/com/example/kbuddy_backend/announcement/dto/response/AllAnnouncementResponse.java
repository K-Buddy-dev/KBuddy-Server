package com.example.kbuddy_backend.announcement.dto.response;

import java.util.List;

public record AllAnnouncementResponse(Long nextId, List<AnnouncementPaginationResponse> results) {
    public static AllAnnouncementResponse of(Long nextId, List<AnnouncementPaginationResponse> results) {
        return new AllAnnouncementResponse(nextId, results);
    }
}
