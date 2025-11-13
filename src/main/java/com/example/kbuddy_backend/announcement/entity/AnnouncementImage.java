package com.example.kbuddy_backend.announcement.entity;

import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.common.constant.ImageFileType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announce_id")
    private Announcement announcement;

    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private ImageFileType fileType;

    private String filePath;
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public AnnouncementImage(Announcement announcement, String imageUrl, ImageFileType fileType, String filePath) {
        this.announcement = announcement;
        this.imageUrl = imageUrl;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public void setAnnouncement(Announcement announcement) {this.announcement = announcement;}
}
