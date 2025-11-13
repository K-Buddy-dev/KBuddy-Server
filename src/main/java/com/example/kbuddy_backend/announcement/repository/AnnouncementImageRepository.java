package com.example.kbuddy_backend.announcement.repository;

import com.example.kbuddy_backend.announcement.entity.AnnouncementImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementImageRepository extends JpaRepository<AnnouncementImage, Long> {
    void deleteByFilePath(String filePath);
}
