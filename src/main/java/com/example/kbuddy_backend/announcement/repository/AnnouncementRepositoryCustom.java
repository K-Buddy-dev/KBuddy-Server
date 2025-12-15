package com.example.kbuddy_backend.announcement.repository;

import com.example.kbuddy_backend.announcement.constant.SortBy;
import com.example.kbuddy_backend.announcement.entity.Announcement;

import java.util.List;

public interface AnnouncementRepositoryCustom {

    List<Announcement> paginationNoOffset(Long announcementId, String title, int pageSize, SortBy sortBy);
}
