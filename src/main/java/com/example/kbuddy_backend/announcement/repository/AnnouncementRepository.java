package com.example.kbuddy_backend.announcement.repository;

import com.example.kbuddy_backend.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, AnnouncementRepositoryCustom {

    // Find Announcements by writer and status
}
